package hyperpaint.zql.exec.select;

import hyperpaint.zql.lang.ZQLException;
import hyperpaint.zql.lang.expression.Expression;
import hyperpaint.zql.lang.expression.ExpressionCollection;
import hyperpaint.zql.lang.expression.ExpressionWrapper;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class SortingExec {
    private Comparator<Object[]> rowComparator = null;

    public SortingExec(Expression expression, Map<String, Integer> columnsNameIndex) {
        switch (expression) {
            case ExpressionCollection expressionCollection -> {
                final var list = expressionCollection.getList();

                for (var item : list) {
                    final SortingExec expression1 = new SortingExec(item, columnsNameIndex);

                    rowComparator = rowComparator != null ? rowComparator.thenComparing(expression1.rowComparator) : expression1.rowComparator;
                }
            }
            case ExpressionWrapper expressionWrapper -> {
                final int index = columnsNameIndex.getOrDefault(expressionWrapper.getWrappedExpression().toName(), -1);

                if (index == -1) {
                    throw new ZQLException("Sorting rule contains non-exist expression: " + expressionWrapper.toZql());
                }

                switch (expressionWrapper.getType()) {
                    case ORDER_BY_ASC -> rowComparator = createComparator(index);
                    case ORDER_BY_DESC -> rowComparator = createComparator(index).reversed();
                    default -> throw new IllegalStateException("Unexpected value: " + expressionWrapper.getType());
                }
            }
            case null -> rowComparator = null;
            default -> throw new IllegalStateException("Unexpected value: " + expression);
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
        if (isNotNull()) {
            rows.sort(rowComparator);
        }
    }

    public boolean isNull() {
        return rowComparator == null;
    }

    public boolean isNotNull() {
        return rowComparator != null;
    }
}
