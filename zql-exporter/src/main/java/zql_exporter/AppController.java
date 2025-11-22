package zql_exporter;

import hyperpaint.zql.exec.ResultSet;
import hyperpaint.zql.exec.Statement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestController
public class AppController {
    private final CuratorFramework curator;
    private final ObjectFactory<Socket> zookeeperSocketFactory;

    private final boolean secretEnabled;
    private final String secretValue;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/query")
    public ResponseEntity<String> query(
            @RequestParam(required = false) String secret,
            @RequestParam String query
    ) throws Exception {
        final var checkSecret = checkSecret(secret);
        if (checkSecret != null) return checkSecret;

        final var resultSet = queryExec(query);
        final var stringBuilder = new StringBuilder();

        for (var column : resultSet.getColumns()) {
            stringBuilder.append(column).append("\t");
        }

        stringBuilder.append("\n");

        for (var row : resultSet.getRows()) {
            for (var item : row) {
                stringBuilder.append(item).append("\t");
            }

            stringBuilder.append("\n");
        }

        return ResponseEntity.status(200).body(stringBuilder.toString());
    }

    private ResultSet queryExec(String query) throws Exception {
        final boolean leader = socketExec(COMMAND_STAT)
                .toLowerCase()
                .lines()
                .filter(s -> s.startsWith("mode: "))
                .findFirst()
                .map(s ->
                        switch (s.substring(6)) {
                            case "leader", "standalone" -> true;
                            case "follower" -> false;
                            default -> {
                                log.warn("ZooKeeper mode is unknown in response to stat command: {}", s.substring(6));
                                yield false;
                            }
                        }
                ).orElseGet(() -> {
                    log.warn("Zookeeper mode is not found in response to stat command");
                    return false;
                });

        if (leader) {
            final Statement statement = Statement.createStatement(query);
            return statement.execute(curator.getZookeeperClient().getZooKeeper());
        } else {
            throw new IllegalStateException("ZooKeeper is not leader");
        }
    }

    @GetMapping("/metrics")
    public ResponseEntity<String> metrics(
            @RequestParam(required = false) String secret
    ) throws Exception {
        final var checkSecret = checkSecret(secret);
        if (checkSecret != null) return checkSecret;

        final var result = socketExec(COMMAND_MNTR)
                .lines()
                .map(s -> {
                    final int delimiterIndex = s.indexOf("\t");
                    if (delimiterIndex != -1) {
                        String key = s.substring(0, delimiterIndex);
                        if (key.startsWith("zk_")) {
                            key = "zookeeper_" + key.substring(3);
                        }

                        final String value = s.substring(delimiterIndex + 1);

                        try {
                            // Отображения как число (в корректном формате)
                            return "# TYPE zql_%s gauge\nzql_%s %s".formatted(key, key, String.valueOf(Float.parseFloat(value)));
                        } catch (NumberFormatException ignored) {
                            // Отобразить как метку
                            return "# TYPE zql_%s gauge\nzql_%s{value=\"%s\"} 1.0".formatted(key, key, value);
                        }
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));

        return ResponseEntity.status(200).body(result);
    }

    /// New in 3.3.0: Print details about serving configuration.
    private static final byte[] COMMAND_CONF = "conf".getBytes(StandardCharsets.UTF_8);
    /// New in 3.3.0: List full connection/session details for all clients connected to this server. Includes information on numbers of packets received/sent, session id, operation latencies, last operation performed, etc...
    private static final byte[] COMMAND_CONS = "cons".getBytes(StandardCharsets.UTF_8);
    /// New in 3.3.0: Reset connection/session statistics for all connections.
    private static final byte[] COMMAND_CRST = "crst".getBytes(StandardCharsets.UTF_8);
    /// Lists the outstanding sessions and ephemeral nodes. This only works on the leader.
    private static final byte[] COMMAND_DUMP = "dump".getBytes(StandardCharsets.UTF_8);
    /// Print details about serving environment
    private static final byte[] COMMAND_ENVI = "envi".getBytes(StandardCharsets.UTF_8);
    /// Tests if server is running in a non-error state. The server will respond with imok if it is running. Otherwise, it will not respond at all.
    private static final byte[] COMMAND_RUOK = "ruok".getBytes(StandardCharsets.UTF_8);
    /// Reset server statistics.
    private static final byte[] COMMAND_SRST = "srst".getBytes(StandardCharsets.UTF_8);
    /// New in 3.3.0: Lists full details for the server.
    private static final byte[] COMMAND_SRVR = "srvr".getBytes(StandardCharsets.UTF_8);
    /// Lists brief details for the server and connected clients.
    private static final byte[] COMMAND_STAT = "stat".getBytes(StandardCharsets.UTF_8);
    /// New in 3.3.0: Lists brief information on watches for the server.
    private static final byte[] COMMAND_WCHS = "wchs".getBytes(StandardCharsets.UTF_8);
    /// New in 3.3.0: Lists detailed information on watches for the server, by session. This outputs a list of sessions(connections) with associated watches (paths). Note, depending on the number of watches this operation may be expensive (ie impact server performance), use it carefully.
    private static final byte[] COMMAND_WCHC = "wchc".getBytes(StandardCharsets.UTF_8);
    /// New in 3.3.0: Lists detailed information on watches for the server, by path. This outputs a list of paths (znodes) with associated sessions. Note, depending on the number of watches this operation may be expensive (ie impact server performance), use it carefully.
    private static final byte[] COMMAND_WCHP = "wchp".getBytes(StandardCharsets.UTF_8);
    /// New in 3.4.0: Outputs a list of variables that could be used for monitoring the health of the cluster.
    private static final byte[] COMMAND_MNTR = "mntr".getBytes(StandardCharsets.UTF_8);

    private String socketExec(byte[] command) throws IOException {
        final Socket zookeeperMonitoringSocket = zookeeperSocketFactory.getObject();

        try (zookeeperMonitoringSocket) {
            zookeeperMonitoringSocket.getOutputStream().write(command);
            zookeeperMonitoringSocket.getOutputStream().flush();
            final byte[] result = zookeeperMonitoringSocket.getInputStream().readAllBytes();
            return new String(result, StandardCharsets.UTF_8);
        }
    }

    private ResponseEntity<String> checkSecret(String secret) {
        if (secretEnabled) {
            if (Objects.isNull(secret)) {
                return ResponseEntity.status(401).build();
            }

            if (!Objects.equals(secretValue, secret)) {
                return ResponseEntity.status(403).build();
            }
        }

        return null;
    }
}
