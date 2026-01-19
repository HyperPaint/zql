package hyperpaint.zql.exec;

import org.apache.zookeeper.ZooKeeper;
import org.junit.jupiter.api.Test;

public class ZQLExecTest {
    @Test
    void run() {
        try (ZooKeeper connection = new ZooKeeper("127.0.0.1:2181", 86400000, null)) {
            final Statement statement = Statement.createStatement("select sum(data) from /, ls//, ls/ls//, ls/ls/ls//");
            final ResultSet resultSet = statement.execute(connection);

            for (var item : resultSet.getColumnNames()) {
                System.out.print(item + "\t");
            }

            System.out.println();

            for (var row : resultSet.getRows()) {
                for (var item : row) {
                    System.out.print(item + "\t");
                }

                System.out.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
