package hyperpaint.zql.exec.select;

import hyperpaint.zql.lang.condition.Condition;
import hyperpaint.zql.lang.condition.ConditionCompositeOfCondition;
import hyperpaint.zql.lang.condition.ConditionCompositeOfExpression;
import hyperpaint.zql.lang.condition.ConditionWrapper;

import java.util.List;
import java.util.Map;
import java.util.Objects;

class FilterExec {
    @FunctionalInterface
    interface FilterEntry {
        boolean run(String path, String data);
    }

    @FunctionalInterface
    interface FilterRow {
        boolean run(Object[] row, Map<String, Integer> columnsNameIndex);
    }

    final FilterEntry filterEntry;
    final FilterRow filterRow;

    private FilterExec(Condition condition) {
        filterEntry = buildFilterEntry(condition);
        filterRow = buildFilterRow(condition);
    }

    public static FilterExec get(Condition condition) {
        if (condition == null) {
            return null;
        }

        return new FilterExec(condition);
    }

    public static void filterEntries(FilterExec exec, Map <String, String> entries) {
        if (exec == null) {
            return;
        }

        entries.entrySet().removeIf(entry -> !exec.filterEntry.run(entry.getKey(), entry.getValue()));
    }

    public static void filterRows(FilterExec exec, List<Object[]> rows, Map<String, Integer> columnsNameIndex) {
        if (exec == null) {
            return;
        }

        rows.removeIf(row -> !exec.filterRow.run(row, columnsNameIndex));
    }

    // region FilterEntry

    static FilterEntry buildFilterEntry(Condition condition) {
        return switch (condition) {
            case ConditionCompositeOfCondition conditionCompositeOfCondition -> buildFilterEntry(conditionCompositeOfCondition);
            case ConditionCompositeOfExpression conditionCompositeOfExpression -> buildFilterEntry(conditionCompositeOfExpression);
            case ConditionWrapper conditionWrapper -> buildFilterEntry(conditionWrapper);
            default -> throw new IllegalStateException("Unexpected value: " + condition);
        };
    }

    private static FilterEntry buildFilterEntry(ConditionCompositeOfCondition condition) {
        final var left = buildFilterEntry(condition.getLeft());
        final var right = buildFilterEntry(condition.getRight());

        return switch (condition.getType()) {
            case AND -> (path, data) -> left.run(path, data) && right.run(path, data);
            case OR -> (path, data) -> left.run(path, data) || right.run(path, data);
            default -> throw new IllegalStateException("Unexpected value: " + condition.getType());
        };
    }

    private static FilterEntry buildFilterEntry(ConditionCompositeOfExpression condition) {
        final var left = ExpressionExec.buildExecEntry(condition.getLeft());
        final var right = ExpressionExec.buildExecEntry(condition.getRight());

        return switch (condition.getType()) {
            case EQUALS -> (path, data) -> Objects.equals(left.run(path, data), right.run(path, data));
            case NOT_EQUALS -> (path, data) -> !Objects.equals(left.run(path, data), right.run(path, data));
            case LIKE -> (path, data) -> Objects.toString(left.run(path, data)).matches(Objects.toString(right.run(path, data)));
            case NOT_LIKE -> (path, data) -> !Objects.toString(left.run(path, data)).matches(Objects.toString(right.run(path, data)));
            default -> throw new IllegalStateException("Unexpected value: " + condition.getType());
        };
    }

    private static FilterEntry buildFilterEntry(ConditionWrapper condition) {
        //noinspection SwitchStatementWithTooFewBranches
        return switch (condition.getType()) {
            case BRACKETS -> buildFilterEntry(condition.getWrappedCondition());
            default -> throw new IllegalStateException("Unexpected value: " + condition.getType());
        };
    }

    // endregion

    // region FilterRow

    static FilterRow buildFilterRow(Condition condition) {
        return switch (condition) {
            case ConditionCompositeOfCondition conditionCompositeOfCondition -> buildFilterRow(conditionCompositeOfCondition);
            case ConditionCompositeOfExpression conditionCompositeOfExpression -> buildFilterRow(conditionCompositeOfExpression);
            case ConditionWrapper conditionWrapper -> buildFilterRow(conditionWrapper);
            default -> throw new IllegalStateException("Unexpected value: " + condition);
        };
    }

    private static FilterRow buildFilterRow(ConditionCompositeOfCondition condition) {
        final var left = buildFilterRow(condition.getLeft());
        final var right = buildFilterRow(condition.getRight());

        return switch (condition.getType()) {
            case AND -> (row, columnsNameIndex) -> left.run(row, columnsNameIndex) && right.run(row, columnsNameIndex);
            case OR -> (row, columnsNameIndex) -> left.run(row, columnsNameIndex) || right.run(row, columnsNameIndex);
            default -> throw new IllegalStateException("Unexpected value: " + condition.getType());
        };
    }

    private static FilterRow buildFilterRow(ConditionCompositeOfExpression condition) {
        final var left = ExpressionExec.buildExecRow(condition.getLeft());
        final var right = ExpressionExec.buildExecRow(condition.getRight());

        return switch (condition.getType()) {
            case EQUALS -> (row, columnsNameIndex) -> Objects.equals(left.run(row, columnsNameIndex), right.run(row, columnsNameIndex));
            case NOT_EQUALS -> (row, columnsNameIndex) -> !Objects.equals(left.run(row, columnsNameIndex), right.run(row, columnsNameIndex));
            case LIKE -> (row, columnsNameIndex) -> Objects.toString(left.run(row, columnsNameIndex)).matches(Objects.toString(right.run(row, columnsNameIndex)));
            case NOT_LIKE -> (row, columnsNameIndex) -> !Objects.toString(left.run(row, columnsNameIndex)).matches(Objects.toString(right.run(row, columnsNameIndex)));
            default -> throw new IllegalStateException("Unexpected value: " + condition.getType());
        };
    }

    private static FilterRow buildFilterRow(ConditionWrapper condition) {
        //noinspection SwitchStatementWithTooFewBranches
        return switch (condition.getType()) {
            case BRACKETS -> buildFilterRow(condition.getWrappedCondition());
            default -> throw new IllegalStateException("Unexpected value: " + condition.getType());
        };
    }

    // endregion FilterRow
}
