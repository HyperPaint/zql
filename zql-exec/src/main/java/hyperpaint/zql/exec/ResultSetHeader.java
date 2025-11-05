package hyperpaint.zql.exec;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.Map;

@Getter
@AllArgsConstructor
public class ResultSetHeader {
    @SuppressWarnings("unchecked")
    static final ResultSetHeader EMPTY = new ResultSetHeader(new String[0], Collections.EMPTY_MAP);

    private final String[] columns;
    private final Map<String, Integer> columnsIndex;

    public String get(int column) throws IndexOutOfBoundsException {
        return columns[column];
    }

    public int get(String column) {
        return columnsIndex.getOrDefault(column, -1);
    }
}
