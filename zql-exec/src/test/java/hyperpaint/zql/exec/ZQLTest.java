package hyperpaint.zql.exec;

import org.junit.jupiter.api.Test;

public class ZQLTest {
    @Test
    void run() {
        ZooKeeperConnectionPool connectionPool = new ZooKeeperConnectionPool("127.0.0.1:2181");
        ZooKeeperConnection connection = connectionPool.getConnection();
        PreparedSelect preparedStatement = connection.prepareStatement("select path as 'mypath', data mydata from /, ls//, ls/ls//, ls/ls/ls// where path like '.+node.+' and data == 1");
        ResultSet resultSet = preparedStatement.executeQuery();
    }
}
