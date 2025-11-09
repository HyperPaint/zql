package hyperpaint.zql.exec;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class ResultSet {
    private String[] columns;
    private Map<String, Integer> columnsIndex;
    private final List<Object[]> rows;
}
