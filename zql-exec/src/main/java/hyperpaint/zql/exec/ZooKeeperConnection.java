package hyperpaint.zql.exec;

import org.apache.zookeeper.ZooKeeper;

public class ZooKeeperConnection {
    private final ZooKeeper connection;

    public ZooKeeperConnection(ZooKeeper connection) {
        this.connection = connection;
    }

    public PreparedSelect prepareStatement(String query) {
        return new PreparedSelect(connection, query);
    }
}
