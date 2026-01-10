package zql_exporter;

import hyperpaint.zql.exec.ResultSet;
import hyperpaint.zql.exec.Statement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.http.HttpStatus;
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

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/query")
    public ResponseEntity<String> query(
            @RequestParam String query,
            String format
    ) throws Exception {
        final var resultSet = exec(query);
        final var stringBuilder = new StringBuilder();

        if (format.equalsIgnoreCase("table")) {
            for (var column : resultSet.getColumnNames()) {
                stringBuilder.append(column).append("\t");
            }

            stringBuilder.append("\n");

            for (var row : resultSet.getRows()) {
                for (var item : row) {
                    stringBuilder.append(item).append("\t");
                }

                stringBuilder.append("\n");
            }
        } else if (format.equalsIgnoreCase("metrics")) {

        } else {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You need to specify format 'table' or 'metrics' in query");
        }


        return ResponseEntity.status(200).body(stringBuilder.toString());
    }

    @GetMapping("/metrics")
    public ResponseEntity<String> metrics() throws Exception {
        final var result = exec(ZooKeeperCommands.MNTR)
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

    private ResultSet exec(String query) throws Exception {
        if (isZooKeeperLeader()) {
            final Statement statement = Statement.createStatement(query);
            return statement.execute(curator.getZookeeperClient().getZooKeeper());
        } else {
            throw new IllegalStateException("ZooKeeper is not leader");
        }
    }

    private String exec(ZooKeeperCommands command) throws IOException {
        final Socket socket = zookeeperSocketFactory.getObject();

        try (socket) {
            socket.getOutputStream().write(command.getBytes());
            socket.getOutputStream().flush();
            final byte[] result = socket.getInputStream().readAllBytes();
            return new String(result, StandardCharsets.UTF_8);
        }
    }

    private boolean isZooKeeperLeader() throws IOException {
        return exec(ZooKeeperCommands.STAT)
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
    }
}
