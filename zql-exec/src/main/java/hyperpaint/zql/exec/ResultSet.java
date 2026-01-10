package hyperpaint.zql.exec;

import hyperpaint.zql.exec.select.GroupingTypes;
import hyperpaint.zql.exec.select.SortingTypes;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class ResultSet {
    private String[] columnNames;
    private Map<String, Integer> columnsNameIndexes;

//    private GroupingTypes[] columnGroupingTypes;
//
//    private SortingTypes[] columnSortingTypes;

    private final List<Object[]> rows;
}
