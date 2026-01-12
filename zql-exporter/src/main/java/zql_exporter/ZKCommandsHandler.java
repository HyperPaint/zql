package zql_exporter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@Slf4j
@AllArgsConstructor
@Service
public class ZKCommandsHandler {
    /// <a href="https://zookeeper.apache.org/doc/r3.4.8/zookeeperAdmin.html#sc_zkCommands">ZooKeeper Commands: The Four Letter Words</a>
    public enum ZKCommands {
        /// New in 3.3.0: Print details about serving configuration.
        CONF("conf".getBytes(StandardCharsets.UTF_8)),
        /// New in 3.3.0: List full connection/session details for all clients connected to this server. Includes information on numbers of packets received/sent, session id, operation latencies, last operation performed, etc...
        CONS("cons".getBytes(StandardCharsets.UTF_8)),
        /// New in 3.3.0: Reset connection/session statistics for all connections.
        CRST("crst".getBytes(StandardCharsets.UTF_8)),
        /// Lists the outstanding sessions and ephemeral nodes. This only works on the leader.
        DUMP("dump".getBytes(StandardCharsets.UTF_8)),
        /// Print details about serving environment
        ENVI("envi".getBytes(StandardCharsets.UTF_8)),
        /// Tests if server is running in a non-error state. The server will respond with imok if it is running. Otherwise, it will not respond at all.
        RUOK("ruok".getBytes(StandardCharsets.UTF_8)),
        /// Reset server statistics.
        SRST("srst".getBytes(StandardCharsets.UTF_8)),
        /// New in 3.3.0: Lists full details for the server.
        SRVR("srvr".getBytes(StandardCharsets.UTF_8)),
        /// Lists brief details for the server and connected clients.
        STAT("stat".getBytes(StandardCharsets.UTF_8)),
        /// New in 3.3.0: Lists brief information on watches for the server.
        WCHS("wchs".getBytes(StandardCharsets.UTF_8)),
        /// New in 3.3.0: Lists detailed information on watches for the server, by session. This outputs a list of sessions(connections) with associated watches (paths). Note, depending on the number of watches this operation may be expensive (ie impact server performance), use it carefully.
        WCHC("wchc".getBytes(StandardCharsets.UTF_8)),
        /// New in 3.3.0: Lists detailed information on watches for the server, by path. This outputs a list of paths (znodes) with associated sessions. Note, depending on the number of watches this operation may be expensive (ie impact server performance), use it carefully.
        WCHP("wchp".getBytes(StandardCharsets.UTF_8)),
        /// New in 3.4.0: Outputs a list of variables that could be used for monitoring the health of the cluster.
        MNTR("mntr".getBytes(StandardCharsets.UTF_8));

        private final @Getter byte[] bytes;

        ZKCommands(byte[] bytes) {
            this.bytes = bytes;
        }
    }

    private final ObjectFactory<Socket> zookeeperSocketFactory;

    public String exec(ZKCommands command) throws IOException {
        final Socket socket = zookeeperSocketFactory.getObject();

        try (socket) {
            socket.getOutputStream().write(command.getBytes());
            socket.getOutputStream().flush();
            final byte[] result = socket.getInputStream().readAllBytes();
            return new String(result, StandardCharsets.UTF_8);
        }
    }

    public boolean isLeader() throws IOException {
        return exec(ZKCommands.STAT)
                .toLowerCase()
                .lines()
                .filter(s -> s.startsWith("mode: "))
                .findFirst()
                .map(s ->
                        switch (s.substring(6)) {
                            case "leader", "standalone" -> true;
                            case "follower" -> false;
                            default -> {
                                log.error("ZooKeeper mode is unknown in response to stat command: {}", s.substring(6));
                                yield false;
                            }
                        }
                ).orElseGet(() -> {
                    log.error("Zookeeper mode is not found in response to stat command");
                    return false;
                });
    }
}
