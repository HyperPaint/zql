package hyperpaint.zql_exporter;

import hyperpaint.zql.exec.ResultSet;
import hyperpaint.zql.exec.Statement;
import hyperpaint.zql.exec.select.GroupingTypes;
import hyperpaint.zql.exec.select.SortingTypes;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@AllArgsConstructor
@Service
class QueryHandler {
    private final ZooKeeper4LetterCommandsHandler zooKeeper4LetterCommandsHandler;
    private final CuratorFramework curator;

    public String handleAsTable(String query) throws Exception {
        return exec(query).toTable();
    }

    public String handleAsMetrics(String query, String help, String type, String name) throws Exception {
        return exec(query).toMetrics(help, type, name);
    }

    public ResultSet exec(String query) throws Exception {
        if (zooKeeper4LetterCommandsHandler.isLeader()) {
            final Statement statement = Statement.createStatement(query);
            return statement.execute(curator.getZookeeperClient().getZooKeeper());
        } else {
            return new ResultSet(new String[0], Collections.emptyMap(), new GroupingTypes[0], new SortingTypes[0], Collections.emptyList());
        }
    }
}
