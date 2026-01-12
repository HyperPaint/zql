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

    // todo metadata
//    private GroupingTypes[] columnGroupingTypes;
//
//    private SortingTypes[] columnSortingTypes;

    private final List<Object[]> rows;

    public String toMetrics(String help, String type, String name) {
        final var stringBuilder = new StringBuilder();

        boolean braceWasOpen, labelWasFound;

        for (var row : rows) {
            for (int i = 0; i < row.length; i++) {
                if (row[i] instanceof Number number) {
                    if (help != null) stringBuilder.append("# HELP zql_").append(name).append("_").append(columnNames[i]).append(" ").append(help).append("\n");
                    stringBuilder.append("# TYPE zql_").append(name).append("_").append(columnNames[i]).append(" ").append(type == null ? "gauge" : type)
                            .append("\nzql_").append(name).append("_").append(columnNames[i]);

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

                            stringBuilder.append(columnNames[j]).append("=\"").append(string).append("\"");

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
}
