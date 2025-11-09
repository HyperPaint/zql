package hyperpaint.zql.exec;

import org.junit.jupiter.api.Test;

public class ZQLExec {
    @Test
    void run() {
        final ZooKeeperConnectionPool connectionPool = new ZooKeeperConnectionPool("127.0.0.1:2181");
        final ZooKeeperConnection connection = connectionPool.getConnection();
        final PreparedSelect preparedStatement = connection.prepareStatement("select path, data from /, ls//, ls/ls//, ls/ls/ls// order by data");
        final ResultSet resultSet = preparedStatement.executeQuery();

        for (var item : resultSet.getColumns()) {
            System.out.print(item + "\t");
        }

        System.out.println();

        for (var row : resultSet.getRows()) {
            for (var item : row) {
                System.out.print(item + "\t");
            }

            System.out.println();
        }
    }
}
