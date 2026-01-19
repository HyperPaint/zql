package hyperpaint.zql.exec;

import hyperpaint.zql.exec.select.SelectStatement;
import hyperpaint.zql.lang.ZQL;
import hyperpaint.zql.lang.ZQLException;
import hyperpaint.zql.lang.statement.Select;
import lombok.NonNull;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

public interface Statement {
    static Statement createStatement(@NonNull String query) throws ZQLException {
        var statement = ZQL.parse(query);
        return switch (statement.getType()) {
            case SELECT -> new SelectStatement((Select) statement);
        };
    }

    ResultSet execute(ZooKeeper connection) throws IOException, ZQLException;
}
