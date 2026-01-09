package hyperpaint.zql.exec.select;

import lombok.Getter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

class SortingExec {
    private Comparator<Object[]> comparator = null;

    private final @Getter boolean skip;

    private SortingExec(SortingType[] columnSortingTypes) {
        skip = Arrays.stream(columnSortingTypes).allMatch(SortingType::isNotSorting);

        if (skip) {
            comparator = null;
        } else {
            for (int i = 0; i < columnSortingTypes.length; i++) {
                switch (columnSortingTypes[i]) {
                    case ASCENDANT -> {
                        if (comparator == null) {
                            comparator = buildExecRow(i);
                        } else {
                            comparator = comparator.thenComparing(buildExecRow(i));
                        }
                    }
                    case DESCENDANT -> {
                        if (comparator == null) {
                            comparator = buildExecRow(i).reversed();
                        } else {
                            comparator = comparator.thenComparing(buildExecRow(i)).reversed();
                        }
                    }
                }
            }
        }
    }

    public static SortingExec get(SortingType[] columnSortingTypes) {
        return new SortingExec(columnSortingTypes);
    }

    public static void sortRows(SortingExec exec, List<Object[]> rows) {
        if (exec.skip) {
            return;
        }

        rows.sort(exec.comparator);
    }

    private Comparator<Object[]> buildExecRow(int index) {
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
}
