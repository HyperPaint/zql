package hyperpaint.zql.exec.select;

import hyperpaint.zql.lang.condition.Condition;
import hyperpaint.zql.lang.condition.ConditionCompositeOfCondition;
import hyperpaint.zql.lang.condition.ConditionCompositeOfExpression;
import hyperpaint.zql.lang.condition.ConditionWrapper;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FilteringExec {
    @FunctionalInterface
    public interface EntryFiltering {
        boolean run(String path, String data);
    }

    @FunctionalInterface
    public interface RowFiltering {
        boolean run(Object[] row, Map<String, Integer> columnsNameIndex);
    }

    private final EntryFiltering entryFiltering;
    private final RowFiltering rowFiltering;

    // todo вынести в функции
    public FilteringExec(Condition condition) {
        switch (condition) {
            case ConditionCompositeOfCondition conditionCompositeOfCondition -> {
                final FilteringExec condition1 = new FilteringExec(conditionCompositeOfCondition.getLeft());
                final FilteringExec condition2 = new FilteringExec(conditionCompositeOfCondition.getRight());

                switch (condition.getType()) {
                    case AND -> {
                        entryFiltering = (path, data) -> condition1.entryFiltering.run(path, data) && condition2.entryFiltering.run(path, data);
                        rowFiltering = (row, columnsNameIndex) -> condition1.rowFiltering.run(row, columnsNameIndex) && condition2.rowFiltering.run(row, columnsNameIndex);
                    }
                    case OR -> {
                        entryFiltering = (path, data) -> condition1.entryFiltering.run(path, data) || condition2.entryFiltering.run(path, data);
                        rowFiltering = (row, columnsNameIndex) -> condition1.rowFiltering.run(row, columnsNameIndex) || condition2.rowFiltering.run(row, columnsNameIndex);
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + condition.getType());
                }
            }
            case ConditionCompositeOfExpression conditionCompositeOfExpression -> {
                final ExpressionExec expression1 = new ExpressionExec(conditionCompositeOfExpression.getLeft());
                final ExpressionExec expression2 = new ExpressionExec(conditionCompositeOfExpression.getRight());

                switch (condition.getType()) {
                    case EQUALS -> {
                        entryFiltering = (path, data) -> {
                            final Object object1 = expression1.execute(path, data);
                            final Object object2 = expression2.execute(path, data);

                            return Objects.equals(object1, object2);
                        };

                        rowFiltering = (row, columnsNameIndex) -> {
                            final Object object1 = expression1.execute(row, columnsNameIndex);
                            final Object object2 = expression2.execute(row, columnsNameIndex);

                            return Objects.equals(object1, object2);
                        };
                    }
                    case NOT_EQUALS -> {
                        entryFiltering = (path, data) -> {
                            final Object object1 = expression1.execute(path, data);
                            final Object object2 = expression2.execute(path, data);

                            return !Objects.equals(object1, object2);
                        };

                        rowFiltering = (row, columnsNameIndex) -> {
                            final Object object1 = expression1.execute(row, columnsNameIndex);
                            final Object object2 = expression2.execute(row, columnsNameIndex);

                            return !Objects.equals(object1, object2);
                        };
                    }
                    case LIKE -> {
                        entryFiltering = (path, data) -> {
                            final Object object1 = expression1.execute(path, data);
                            final Object object2 = expression2.execute(path, data);

                            if (Objects.isNull(object1) || Objects.isNull(object2)) {
                                return false;
                            }

                            return object1.toString().matches(object2.toString());
                        };

                        rowFiltering = (row, columnsNameIndex) -> {
                            final Object object1 = expression1.execute(row, columnsNameIndex);
                            final Object object2 = expression2.execute(row, columnsNameIndex);

                            if (Objects.isNull(object1) || Objects.isNull(object2)) {
                                return false;
                            }

                            return object1.toString().matches(object2.toString());
                        };
                    }
                    case NOT_LIKE -> {
                        entryFiltering = (path, data) -> {
                            final Object object1 = expression1.execute(path, data);
                            final Object object2 = expression2.execute(path, data);

                            if (Objects.isNull(object1) || Objects.isNull(object2)) {
                                return false;
                            }

                            return !object1.toString().matches(object2.toString());
                        };

                        rowFiltering = (row, columnsNameIndex) -> {
                            final Object object1 = expression1.execute(row, columnsNameIndex);
                            final Object object2 = expression2.execute(row, columnsNameIndex);

                            if (Objects.isNull(object1) || Objects.isNull(object2)) {
                                return false;
                            }

                            return !object1.toString().matches(object2.toString());
                        };
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + condition.getType());
                }
            }
            case ConditionWrapper conditionWrapper -> {
                final FilteringExec condition1 = new FilteringExec(conditionWrapper.getWrappedCondition());

                //noinspection SwitchStatementWithTooFewBranches
                switch (condition.getType()) {
                    case BRACKETS -> {
                        entryFiltering = condition1.entryFiltering;
                        rowFiltering = condition1.rowFiltering;
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + condition.getType());
                }
            }
            case null -> {
                entryFiltering = null;
                rowFiltering = null;
            }
            default -> throw new IllegalArgumentException("Unexpected value: " + condition);
        }
    }

    public void execute(Map<String, String> entries) {
        if (isNotNull()) {
            entries.entrySet().removeIf(entry -> !entryFiltering.run(entry.getKey(), entry.getValue()));
        }
    }

    public void execute(List<Object[]> rows, Map<String, Integer> columnsNameIndex) {
        if (isNotNull()) {
            rows.removeIf(row -> !rowFiltering.run(row, columnsNameIndex));
        }
    }

    public boolean isNull() {
        return entryFiltering == null || rowFiltering == null;
    }

    public boolean isNotNull() {
        return entryFiltering != null && rowFiltering != null;
    }
}
