package zql_exporter;

import hyperpaint.zql.exec.ResultSet;
import hyperpaint.zql.exec.Statement;
import hyperpaint.zql.lang.ZQLException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.stereotype.Service;
import zql_exporter.config.ZqlExporterConfig;

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
        return exec(query).toMetrics(help, type, name);
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
