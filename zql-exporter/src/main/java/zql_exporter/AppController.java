package zql_exporter;

import hyperpaint.zql.exec.ResultSet;
import hyperpaint.zql.exec.Statement;
import hyperpaint.zql.lang.ZQLException;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

@Slf4j
@RestController
public class AppController {
    private final Object zookeeperLock = new Object();

    private final ObjectFactory<ZooKeeper> zookeeperFactory;
    private final ObjectFactory<Socket> zookeeperMonitoringSocketFactory;

    private volatile ZooKeeper zookeeper;
    private int reconnectionAttempt = 0;

    public AppController(ObjectFactory<ZooKeeper> zookeeperFactory, ObjectFactory<Socket> zookeeperMonitoringSocketFactory) {
        this.zookeeperFactory = zookeeperFactory;
        this.zookeeperMonitoringSocketFactory = zookeeperMonitoringSocketFactory;

        this.zookeeper = zookeeperFactory.getObject();
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/query")
    public ResponseEntity<String> query(@RequestParam String query, @RequestParam(required = false) String output) {
        try {
            final var resultSet = queryHandle(query);
            final var stringBuilder = new StringBuilder();

            if (Objects.equals(output, "table")) {
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
            } else if (Objects.equals(output, "metrics")) {
                return ResponseEntity.status(501).build();
            } else {
                return ResponseEntity.status(200).build();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    private ResultSet queryHandle(String query) throws IllegalStateException, ZQLException {
        /* Переподключить если не подключен */
        if (!zookeeper.getState().isAlive()) {
            synchronized (zookeeperLock) {
                if (!zookeeper.getState().isAlive()) {
                    log.error("Connection state to zookeeper is {}", zookeeper.getState());

                    log.warn("Zookeeper reconnection attempt #{}", ++reconnectionAttempt);
                    zookeeper = zookeeperFactory.getObject();

                    log.warn("Connection state to zookeeper is {}", zookeeper.getState());
                }
            }
        }

        /* Обработать запрос если подключен */
        if (zookeeper.getState().isConnected()) {
            synchronized (zookeeperLock) {
                if (zookeeper.getState().isConnected()) {
                    if (isZookeeperLeader()) {
                        final Statement statement = Statement.createStatement(query);
                        return statement.execute(zookeeper);
                    } else {
                        throw new IllegalStateException("ZooKeeper is not leader");
                    }
                }
            }
        }

        throw new IllegalStateException("Zookeeper is not connected");
    }

    private boolean isZookeeperLeader() {
        return socketHandle("stat")
                .lines()
                .filter(s -> s.startsWith("Mode: "))
                .findAny()
                .map(s ->
                        switch (s.substring(6)) {
                            case "leader", "standalone" -> true;
                            case "follower" -> false;
                            default -> {
                                log.warn("ZooKeeper mode is unknown: {}", s.substring(6));
                                yield  false;
                            }
                        }
                ).orElseGet(() -> {
                    log.warn("Zookeeper mode is not found");
                    return false;
                });
    }

    @GetMapping("/socket")
    public ResponseEntity<String> socket(@RequestParam String command) {
        try {
            return ResponseEntity.status(200).body(socketHandle(command));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    private String socketHandle(String command) {
        final Socket zookeeperMonitoringSocket = zookeeperMonitoringSocketFactory.getObject();

        try (zookeeperMonitoringSocket) {
            zookeeperMonitoringSocket.getOutputStream().write("%s\r\n".formatted(command).getBytes(StandardCharsets.UTF_8));
            zookeeperMonitoringSocket.getOutputStream().flush();
            return new String(zookeeperMonitoringSocket.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error(e.toString(), e);
            return e + "\n" + Arrays.toString(e.getStackTrace());
        }
    }
}
