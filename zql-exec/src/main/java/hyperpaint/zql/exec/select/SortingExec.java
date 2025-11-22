package hyperpaint.zql.exec.select;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

class SortingExec {
    private Comparator<Object[]> rowComparator = null;

    SortingExec(SortingType[] columnSortingTypes) {
        if (Arrays.stream(columnSortingTypes).allMatch(type -> type == SortingType.NONE)) {
            return;
        }

        for (int i = 0; i < columnSortingTypes.length; i++) {
            switch (columnSortingTypes[i]) {
                case ASCENDANT -> {
                    if (rowComparator == null) {
                        rowComparator = createComparator(i);
                    } else {
                        rowComparator = rowComparator.thenComparing(createComparator(i));
                    }
                }
                case DESCENDANT -> {
                    if (rowComparator == null) {
                        rowComparator = createComparator(i).reversed();
                    } else {
                        rowComparator = rowComparator.thenComparing(createComparator(i)).reversed();
                    }
                }
            }
        }
    }

    private Comparator<Object[]> createComparator(int index) {
        return (first, second) -> {
            if (first[index] != null) {
                if (second[index] != null) {
                    return switch (first[index]) {
                        // int
                        case Integer int1 when second[index] instanceof Integer int2 -> Integer.compare(int1, int2);
                        case Integer int1 when second[index] instanceof Float float2 -> Float.compare(int1, float2);
                        case Integer ignored1 when second[index] instanceof String ignored2 -> -1; // string в конец
                        // float
                        case Float float1 when second[index] instanceof Integer int2 -> Float.compare(float1, int2);
                        case Float float1 when second[index] instanceof Float float2 -> Float.compare(float1, float2);
                        case Float ignored1 when second[index] instanceof String ignored2 -> -1; // string в конец
                        // string
                        case String ignored1 when second[index] instanceof Integer ignored2 -> 1; // string в конец
                        case String ignored1 when second[index] instanceof Float ignored2 -> 1; // string в конец
                        case String string1 when second[index] instanceof String string2 -> string1.compareTo(string2);
                        // default
                        default -> 0;
                    };
                } else {
                    return -1; // null в конец
                }
            } else {
                if (second[index] != null) {
                    return 1; // null в конец
                } else {
                    return 0;
                }
            }
        };
    }

    public void execute(List<Object[]> rows) {
        if (rowComparator != null) {
            rows.sort(rowComparator);
        }
    }
}
