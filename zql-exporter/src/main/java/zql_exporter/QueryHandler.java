package zql_exporter;

import hyperpaint.zql.exec.ResultSet;
import hyperpaint.zql.exec.Statement;
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

    public ResponseEntity<String> queries() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(HttpStatus.NOT_FOUND.getReasonPhrase());
    }

    public ResponseEntity<String> handleAsTable(String query) throws Exception {
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

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.TEXT_PLAIN).body(stringBuilder.toString());
    }

    public ResponseEntity<String> handleAsMetrics(String query) throws Exception {
        final var resultSet = exec(query);

        final var columns = resultSet.getColumnNames();
        final var rows = resultSet.getRows();

        final var stringBuilder = new StringBuilder();

        for (var row : rows) {
            for (int i = 0; i < row.length; i++) {
                if (row[i] instanceof Number number) {
                    stringBuilder.append("# TYPE zql_%s gauge\nzql_%s_").append(columns[i]).append("{");

                    for (int j = 0; j < row.length; j++) {
                        if (row[j] instanceof String string) {
                            if (j != 0) {
                                stringBuilder.append(",");
                            }

                            stringBuilder.append(columns[j]).append("=\"").append(string).append("\"");
                        }
                    }

                    stringBuilder.append("}").append(" ").append(number.floatValue()).append("\n");
                }
            }
        }

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.TEXT_PLAIN).body(stringBuilder.toString());
    }

    public ResultSet exec(String query) throws Exception {
        if (zkCommandsHandler.isLeader()) {
            final Statement statement = Statement.createStatement(query);
            return statement.execute(curator.getZookeeperClient().getZooKeeper());
        } else {
            throw new IllegalStateException("ZooKeeper is not leader");
        }
    }
}
