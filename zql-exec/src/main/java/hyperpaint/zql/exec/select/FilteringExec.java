package hyperpaint.zql.exec.select;

import hyperpaint.zql.lang.condition.Condition;
import hyperpaint.zql.lang.condition.ConditionCompositeOfCondition;
import hyperpaint.zql.lang.condition.ConditionCompositeOfExpression;
import hyperpaint.zql.lang.condition.ConditionWrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
class FilteringExec {
    @FunctionalInterface
    interface FilteringEntry {
        boolean run(String path, String data);
    }

    @FunctionalInterface
    interface FilteringRow {
        boolean run(Object[] row, Map<String, Integer> columnsNameIndex);
    }

    final FilteringEntry filteringEntry;
    final FilteringRow filteringRow;

    private FilteringExec(Condition condition) {
        filteringEntry = buildFilterEntry(condition);
        filteringRow = buildFilterRow(condition);
    }

    public static FilteringExec get(Condition condition) {
        if (condition == null) {
            return null;
        }

        return new FilteringExec(condition);
    }

    public static void filterEntries(FilteringExec exec, Map <String, String> entries) {
        if (exec == null) {
            return;
        }

        final long startMilliseconds = System.currentTimeMillis();

        entries.entrySet().removeIf(entry -> !exec.filteringEntry.run(entry.getKey(), entry.getValue()));

        final long diffMilliseconds = System.currentTimeMillis() - startMilliseconds;
        if (log.isDebugEnabled()) {
            log.debug("Filtering entries took {} millis", diffMilliseconds);
        } else if (diffMilliseconds > 1000) {
            log.warn("Filtering entries took too long: {} millis", diffMilliseconds);
        }
    }

    public static void filterRows(FilteringExec exec, List<Object[]> rows, Map<String, Integer> columnsNameIndex) {
        if (exec == null) {
            return;
        }

        final long startMilliseconds = System.currentTimeMillis();

        rows.removeIf(row -> !exec.filteringRow.run(row, columnsNameIndex));

        final long diffMilliseconds = System.currentTimeMillis() - startMilliseconds;
        if (log.isDebugEnabled()) {
            log.debug("Filtering rows took {} millis", diffMilliseconds);
        } else if (diffMilliseconds > 1000) {
            log.warn("Filtering rows took too long: {} millis", diffMilliseconds);
        }
    }

    // region FilterEntry

    static FilteringEntry buildFilterEntry(Condition condition) {
        return switch (condition) {
            case ConditionCompositeOfCondition conditionCompositeOfCondition -> buildFilterEntry(conditionCompositeOfCondition);
            case ConditionCompositeOfExpression conditionCompositeOfExpression -> buildFilterEntry(conditionCompositeOfExpression);
            case ConditionWrapper conditionWrapper -> buildFilterEntry(conditionWrapper);
            default -> throw new IllegalStateException("Unexpected value: " + condition);
        };
    }

    private static FilteringEntry buildFilterEntry(ConditionCompositeOfCondition condition) {
        final var left = buildFilterEntry(condition.getLeft());
        final var right = buildFilterEntry(condition.getRight());

        return switch (condition.getType()) {
            case C_C_AND -> (path, data) -> left.run(path, data) && right.run(path, data);
            case C_C_OR -> (path, data) -> left.run(path, data) || right.run(path, data);
            default -> throw new IllegalStateException("Unexpected value: " + condition.getType());
        };
    }

    private static FilteringEntry buildFilterEntry(ConditionCompositeOfExpression condition) {
        final var left = ExpressionExec.buildExecEntry(condition.getLeft());
        final var right = ExpressionExec.buildExecEntry(condition.getRight());

        return switch (condition.getType()) {
            case E_E_GREATER -> (path, data) -> {
                final float leftFloat;
                final var leftObject = left.run(path, data);
                if (leftObject instanceof Number number) {
                    leftFloat = number.floatValue();
                } else {
                    try {
                        leftFloat = Float.parseFloat(leftObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert left part of greater than to number, it is not number: {}, skipping...", leftObject);
                        return false;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert left part of greater than to number, it is null: {}, skipping...", leftObject);
                        return false;
                    }
                }

                final float rightFloat;
                final var rightObject = right.run(path, data);
                if (leftObject instanceof Number number) {
                    rightFloat = number.floatValue();
                } else {
                    try {
                        rightFloat = Float.parseFloat(rightObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert right part of greater than to number, it is not number: {}, skipping...", rightObject);
                        return false;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert right part of greater than to number, it is null: {}, skipping...", rightObject);
                        return false;
                    }
                }

                return leftFloat > rightFloat;
            };
            case E_E_GREATER_EQUALS -> (path, data) -> {
                final float leftFloat;
                final var leftObject = left.run(path, data);
                if (leftObject instanceof Number number) {
                    leftFloat = number.floatValue();
                } else {
                    try {
                        leftFloat = Float.parseFloat(leftObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert left part of greater than or equals to number, it is not number: {}, skipping...", leftObject);
                        return false;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert left part of greater than or equals to number, it is null: {}, skipping...", leftObject);
                        return false;
                    }
                }

                final float rightFloat;
                final var rightObject = right.run(path, data);
                if (leftObject instanceof Number number) {
                    rightFloat = number.floatValue();
                } else {
                    try {
                        rightFloat = Float.parseFloat(rightObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert right part of greater than or equals to number, it is not number: {}, skipping...", rightObject);
                        return false;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert right part of greater than or equals to number, it is null: {}, skipping...", rightObject);
                        return false;
                    }
                }

                return leftFloat >= rightFloat;
            };
            case E_E_LOWER -> (path, data) -> {
                final float leftFloat;
                final var leftObject = left.run(path, data);
                if (leftObject instanceof Number number) {
                    leftFloat = number.floatValue();
                } else {
                    try {
                        leftFloat = Float.parseFloat(leftObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert left part of less than to number, it is not number: {}, skipping...", leftObject);
                        return false;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert left part of less than to number, it is null: {}, skipping...", leftObject);
                        return false;
                    }
                }

                final float rightFloat;
                final var rightObject = right.run(path, data);
                if (leftObject instanceof Number number) {
                    rightFloat = number.floatValue();
                } else {
                    try {
                        rightFloat = Float.parseFloat(rightObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert right part of less than to number, it is not number: {}, skipping...", rightObject);
                        return false;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert right part of less than to number, it is null: {}, skipping...", rightObject);
                        return false;
                    }
                }

                return leftFloat < rightFloat;
            };
            case E_E_LOWER_EQUALS -> (path, data) -> {
                final float leftFloat;
                final var leftObject = left.run(path, data);
                if (leftObject instanceof Number number) {
                    leftFloat = number.floatValue();
                } else {
                    try {
                        leftFloat = Float.parseFloat(leftObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert left part of less than or equals to number, it is not number: {}, skipping...", leftObject);
                        return false;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert left part of less than or equals to number, it is null: {}, skipping...", leftObject);
                        return false;
                    }
                }

                final float rightFloat;
                final var rightObject = right.run(path, data);
                if (leftObject instanceof Number number) {
                    rightFloat = number.floatValue();
                } else {
                    try {
                        rightFloat = Float.parseFloat(rightObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert right part of less than or equals to number, it is not number: {}, skipping...", rightObject);
                        return false;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert right part of less than or equals to number, it is null: {}, skipping...", rightObject);
                        return false;
                    }
                }

                return leftFloat <= rightFloat;
            };
            case E_E_EQUALS -> (path, data) -> Objects.equals(left.run(path, data), right.run(path, data));
            case E_E_NOT_EQUALS -> (path, data) -> !Objects.equals(left.run(path, data), right.run(path, data));
            case E_E_LIKE -> (path, data) -> Objects.toString(left.run(path, data)).matches(Objects.toString(right.run(path, data)));
            case E_E_NOT_LIKE -> (path, data) -> !Objects.toString(left.run(path, data)).matches(Objects.toString(right.run(path, data)));
            default -> throw new IllegalStateException("Unexpected value: " + condition.getType());
        };
    }

    private static FilteringEntry buildFilterEntry(ConditionWrapper condition) {
        //noinspection SwitchStatementWithTooFewBranches
        return switch (condition.getType()) {
            case C_C_WRAP -> buildFilterEntry(condition.getWrappedCondition());
            default -> throw new IllegalStateException("Unexpected value: " + condition.getType());
        };
    }

    // endregion

    // region FilterRow

    static FilteringRow buildFilterRow(Condition condition) {
        return switch (condition) {
            case ConditionCompositeOfCondition conditionCompositeOfCondition -> buildFilterRow(conditionCompositeOfCondition);
            case ConditionCompositeOfExpression conditionCompositeOfExpression -> buildFilterRow(conditionCompositeOfExpression);
            case ConditionWrapper conditionWrapper -> buildFilterRow(conditionWrapper);
            default -> throw new IllegalStateException("Unexpected value: " + condition);
        };
    }

    private static FilteringRow buildFilterRow(ConditionCompositeOfCondition condition) {
        final var left = buildFilterRow(condition.getLeft());
        final var right = buildFilterRow(condition.getRight());

        return switch (condition.getType()) {
            case C_C_AND -> (row, columnsNameIndex) -> left.run(row, columnsNameIndex) && right.run(row, columnsNameIndex);
            case C_C_OR -> (row, columnsNameIndex) -> left.run(row, columnsNameIndex) || right.run(row, columnsNameIndex);
            default -> throw new IllegalStateException("Unexpected value: " + condition.getType());
        };
    }

    private static FilteringRow buildFilterRow(ConditionCompositeOfExpression condition) {
        final var left = ExpressionExec.buildExecRow(condition.getLeft());
        final var right = ExpressionExec.buildExecRow(condition.getRight());

        return switch (condition.getType()) {
            case E_E_GREATER -> (row, columnsNameIndex) -> {
                final float leftFloat;
                final var leftObject = left.run(row, columnsNameIndex);
                if (leftObject instanceof Number number) {
                    leftFloat = number.floatValue();
                } else {
                    try {
                        leftFloat = Float.parseFloat(leftObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert left part of greater than to number, it is not number: {}, skipping...", leftObject);
                        return false;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert left part of greater than to number, it is null: {}, skipping...", leftObject);
                        return false;
                    }
                }

                final float rightFloat;
                final var rightObject = right.run(row, columnsNameIndex);
                if (leftObject instanceof Number number) {
                    rightFloat = number.floatValue();
                } else {
                    try {
                        rightFloat = Float.parseFloat(rightObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert right part of greater than to number, it is not number: {}, skipping...", rightObject);
                        return false;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert right part of greater than to number, it is null: {}, skipping...", rightObject);
                        return false;
                    }
                }

                return leftFloat > rightFloat;
            };
            case E_E_GREATER_EQUALS -> (row, columnsNameIndex) -> {
                final float leftFloat;
                final var leftObject = left.run(row, columnsNameIndex);
                if (leftObject instanceof Number number) {
                    leftFloat = number.floatValue();
                } else {
                    try {
                        leftFloat = Float.parseFloat(leftObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert left part of greater than or equals to number, it is not number: {}, skipping...", leftObject);
                        return false;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert left part of greater than or equals to number, it is null: {}, skipping...", leftObject);
                        return false;
                    }
                }

                final float rightFloat;
                final var rightObject = right.run(row, columnsNameIndex);
                if (leftObject instanceof Number number) {
                    rightFloat = number.floatValue();
                } else {
                    try {
                        rightFloat = Float.parseFloat(rightObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert right part of greater than or equals to number, it is not number: {}, skipping...", rightObject);
                        return false;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert right part of greater than or equals to number, it is null: {}, skipping...", rightObject);
                        return false;
                    }
                }

                return leftFloat >= rightFloat;
            };
            case E_E_LOWER -> (row, columnsNameIndex) -> {
                final float leftFloat;
                final var leftObject = left.run(row, columnsNameIndex);
                if (leftObject instanceof Number number) {
                    leftFloat = number.floatValue();
                } else {
                    try {
                        leftFloat = Float.parseFloat(leftObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert left part of less than to number, it is not number: {}, skipping...", leftObject);
                        return false;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert left part of less than to number, it is null: {}, skipping...", leftObject);
                        return false;
                    }
                }

                final float rightFloat;
                final var rightObject = right.run(row, columnsNameIndex);
                if (leftObject instanceof Number number) {
                    rightFloat = number.floatValue();
                } else {
                    try {
                        rightFloat = Float.parseFloat(rightObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert right part of less than to number, it is not number: {}, skipping...", rightObject);
                        return false;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert right part of less than to number, it is null: {}, skipping...", rightObject);
                        return false;
                    }
                }

                return leftFloat < rightFloat;
            };
            case E_E_LOWER_EQUALS -> (row, columnsNameIndex) -> {
                final float leftFloat;
                final var leftObject = left.run(row, columnsNameIndex);
                if (leftObject instanceof Number number) {
                    leftFloat = number.floatValue();
                } else {
                    try {
                        leftFloat = Float.parseFloat(leftObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert left part of less than or equals to number, it is not number: {}, skipping...", leftObject);
                        return false;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert left part of less than or equals to number, it is null: {}, skipping...", leftObject);
                        return false;
                    }
                }

                final float rightFloat;
                final var rightObject = right.run(row, columnsNameIndex);
                if (leftObject instanceof Number number) {
                    rightFloat = number.floatValue();
                } else {
                    try {
                        rightFloat = Float.parseFloat(rightObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert right part of less than or equals to number, it is not number: {}, skipping...", rightObject);
                        return false;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert right part of less than or equals to number, it is null: {}, skipping...", rightObject);
                        return false;
                    }
                }

                return leftFloat <= rightFloat;
            };
            case E_E_EQUALS -> (row, columnsNameIndex) -> Objects.equals(left.run(row, columnsNameIndex), right.run(row, columnsNameIndex));
            case E_E_NOT_EQUALS -> (row, columnsNameIndex) -> !Objects.equals(left.run(row, columnsNameIndex), right.run(row, columnsNameIndex));
            case E_E_LIKE -> (row, columnsNameIndex) -> Objects.toString(left.run(row, columnsNameIndex)).matches(Objects.toString(right.run(row, columnsNameIndex)));
            case E_E_NOT_LIKE -> (row, columnsNameIndex) -> !Objects.toString(left.run(row, columnsNameIndex)).matches(Objects.toString(right.run(row, columnsNameIndex)));
            default -> throw new IllegalStateException("Unexpected value: " + condition.getType());
        };
    }

    private static FilteringRow buildFilterRow(ConditionWrapper condition) {
        //noinspection SwitchStatementWithTooFewBranches
        return switch (condition.getType()) {
            case C_C_WRAP -> buildFilterRow(condition.getWrappedCondition());
            default -> throw new IllegalStateException("Unexpected value: " + condition.getType());
        };
    }

    // endregion FilterRow
}
