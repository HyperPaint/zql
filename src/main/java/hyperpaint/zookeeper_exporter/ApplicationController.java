package hyperpaint.zookeeper_exporter;

import hyperpaint.zql.statement.PreparedStatement;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@Slf4j
@RestController
@AllArgsConstructor
public class ApplicationController {
    @Autowired
    private final CuratorFramework curator;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/metrics")
    public String metrics() {
        final String query = "select path, max(data), min(data) from list('/my') where path like '.*node[0-9].*' group by path";

        try {
            final var preparedStatement = new PreparedStatement(curator, query);
            final var resultSet = preparedStatement.execute();
            return resultSetToTable(resultSet);
        } catch (Exception e) {
            log.error(e.toString(), e);
            return e + Arrays.toString(e.getStackTrace());
        }
    }

    private static String resultSetToTable(PreparedStatement.ResultSet resultSet) {
        final var stringBuilder = new StringBuilder();

        for (var col : resultSet.getColumns()) {
            stringBuilder.append(col);
            stringBuilder.append("\t");
        }
        stringBuilder.append("\n");
        for (var row : resultSet.getRows()) {
            for (int columnIndex = 0; columnIndex < resultSet.getColumns().size(); columnIndex++) {
                stringBuilder.append(row.get(columnIndex));
                stringBuilder.append("\t");
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
