package zql_exporter;

import hyperpaint.zql.exec.ResultSet;
import hyperpaint.zql.exec.Statement;
import hyperpaint.zql.lang.ZQLException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
class QueryHandler {
    private final CuratorFramework curator;
    private final ZKCommandsHandler zkCommandsHandler;

    public String handleAsTable(String query) throws Exception {
        final var resultSet = exec(query);

        final var columns = resultSet.getColumnNames();
        final var rows = resultSet.getRows();

        final var stringBuilder = new StringBuilder();

        for (var column : columns) {
            stringBuilder.append(column).append("\t");
        }

        stringBuilder.append("\n");

        for (var row : rows) {
            for (var item : row) {
                stringBuilder.append(item).append("\t");
            }

            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }

    public String handleAsMetrics(String query, String help, String type, String name) throws Exception {
        final var resultSet = exec(query);

        final var columns = resultSet.getColumnNames();
        final var rows = resultSet.getRows();

        final var stringBuilder = new StringBuilder();

        boolean braceWasOpen, labelWasFound;

        for (var row : rows) {
            for (int i = 0; i < row.length; i++) {
                if (row[i] instanceof Number number) {
                    if (help != null) {
                        stringBuilder.append("# HELP zql_").append(name).append("_").append(columns[i]).append(" ").append(help).append("\n");
                    }

                    stringBuilder.append("# TYPE zql_").append(name).append("_").append(columns[i]).append(" ").append(type).append("\nzql_").append(name).append("_").append(columns[i]);

                    braceWasOpen = false;
                    labelWasFound = false;

                    for (int j = 0; j < row.length; j++) {
                        if (row[j] instanceof String string) {
                            if (!braceWasOpen) {
                                stringBuilder.append("{");
                                braceWasOpen = true;
                            }

                            if (labelWasFound) {
                                stringBuilder.append(",");
                            }

                            stringBuilder.append(columns[j]).append("=\"").append(string).append("\"");

                            labelWasFound = true;
                        }
                    }

                    if (braceWasOpen) {
                        stringBuilder.append("}");
                    }

                    stringBuilder.append(" ").append(number.floatValue()).append("\n");
                }
            }
        }

        return stringBuilder.toString();
    }

    public String queries() {
        return "";
    }

    public ResultSet exec(String query) throws Exception {
        if (zkCommandsHandler.isLeader()) {
            final Statement statement = Statement.createStatement(query);
            return statement.execute(curator.getZookeeperClient().getZooKeeper());
        } else {
            throw new ZQLException("ZooKeeper is not leader");
        }
    }
}
