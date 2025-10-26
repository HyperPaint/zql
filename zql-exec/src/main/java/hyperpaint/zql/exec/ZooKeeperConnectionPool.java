package hyperpaint.zql.exec;

import hyperpaint.zql.lang.ZQLException;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

public class ZooKeeperConnectionPool {
    private final ZooKeeper zooKeeper;

    public ZooKeeperConnectionPool(String connectionString) throws ZQLException {
        try {
            zooKeeper = new ZooKeeper(connectionString, 86400000, null);
        } catch (IOException e) {
            throw new ZQLException(e);
        }
    }

    public ZooKeeperConnection getConnection() {
        return new ZooKeeperConnection(zooKeeper);
    }
}
