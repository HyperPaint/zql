package hyperpaint.zql.exec;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class ResultSet {
    private final Map<String, Integer> columns;
    private final List<Object[]> rows;
}
