package zql_exporter;

import hyperpaint.zql.exec.ResultSet;
import hyperpaint.zql.exec.Statement;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@Slf4j
@RestController
@AllArgsConstructor
public class ApplicationController {
    @Autowired
    private final ZooKeeper zookeeperConnection;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/metrics")
    public String metrics() {
        final String query = "select path, max(data), min(data) from ls//my where path like '.*node[0-9].*' group by path";

        try {
            final Statement statement = Statement.createStatement(query);
            final ResultSet resultSet = statement.execute(zookeeperConnection);
            return resultSetToTable(resultSet);
        } catch (Exception e) {
            log.error(e.toString(), e);
            return e + Arrays.toString(e.getStackTrace());
        }
    }

    private static String resultSetToTable(ResultSet resultSet) {
        final var stringBuilder = new StringBuilder();

        for (var item : resultSet.getColumns()) {
            stringBuilder.append(item).append("\t");
        }

        stringBuilder.append("\n");

        for (var row : resultSet.getRows()) {
            for (var item : row) {
                stringBuilder.append(item).append("\t");
            }

            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }
}
