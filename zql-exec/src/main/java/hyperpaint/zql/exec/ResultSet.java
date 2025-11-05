package hyperpaint.zql.exec;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
@AllArgsConstructor
public class ResultSet {
    @SuppressWarnings("unchecked")
    static final ResultSet EMPTY = new ResultSet(ResultSetHeader.EMPTY, Collections.EMPTY_LIST);

    private final ResultSetHeader header;
    private final List<Object[]> rows;
}
