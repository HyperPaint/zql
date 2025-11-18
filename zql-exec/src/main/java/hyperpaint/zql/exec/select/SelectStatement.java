package hyperpaint.zql.exec.select;

import hyperpaint.zql.exec.ResultSet;
import hyperpaint.zql.exec.Statement;
import hyperpaint.zql.lang.ZQLException;
import hyperpaint.zql.lang.expression.Expression;
import hyperpaint.zql.lang.expression.ExpressionWrapper2;
import hyperpaint.zql.lang.statement.Select;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.zookeeper.ZooKeeper;

import java.util.*;
import java.util.stream.IntStream;

public class SelectStatement implements Statement {
    private final Expression[] selectExpressions;
    private final Expression[] groupByExpressions;

    private final ExpressionExec expressionExec;
    private final ZnodeExec znodeExec;
    private final FilteringExec whereFilteringExec;
    private final GroupingExec groupingExec;
    private final FilteringExec havingFilteringExec;
    private final SortingExec sortingExec;

    /** Наименования полей */
    private String[] columnsName;
    /** Индексы наименований полей */
    private Map<String, Integer> columnsNameIndex;

    /** Тип выражений в полях */
    private Expression.Type[] columnsExpressionType;

    /** Индексы группировки полей */
    private int[] groupingIndex;

    SelectStatement(@NonNull Select select) {
        // region select exceptions

        if (!select.hasSelectExpression() && select.hasFromZnode()) {
            throw new ZQLException("Statement not contains select expressions but contains from znodes");
        }

        if (!select.hasSelectExpression() && select.hasWhereCondition()) {
            throw new ZQLException("Statement not contains select expressions but contains where conditions");
        }

        if (!select.hasSelectExpression() && select.hasGroupByExpression()) {
            throw new ZQLException("Statement not contains select expressions but contains group by expressions");
        }

        if (!select.hasSelectExpression() && select.hasOrderByExpression()) {
            throw new ZQLException("Statement not contains select expressions but contains order by expressions");
        }

        if (!select.hasSelectExpression() && select.hasHavingCondition()) {
            throw new ZQLException("Statement not contains select expressions but contains having conditions");
        }

        // endregion

        // region from znode exceptions

        if (!select.hasFromZnode() && select.hasWhereCondition()) {
            throw new ZQLException("Statement not contains from znode but contains where conditions");
        }

        if (!select.hasFromZnode() && select.hasGroupByExpression()) {
            throw new ZQLException("Statement not contains from znode but contains group by expressions");
        }

        if (!select.hasFromZnode() && select.hasHavingCondition()) {
            throw new ZQLException("Statement not contains from znode but contains having conditions");
        }

        if (!select.hasFromZnode() && select.hasOrderByExpression()) {
            throw new ZQLException("Statement not contains from znode but contains order by expressions");
        }

        // endregion

        // region group by exceptions

        if (!select.hasGroupByExpression() && select.hasHavingCondition()) {
            throw new ZQLException("Statement not contains group by expressions but contains having conditions");
        }

        // endregion

        // region analyze

        selectExpressions = select.hasSelectExpression() ? select.getSelectExpression().toComponents() : null;
        expressionExec = new ExpressionExec(select.getSelectExpression());
        analyzeSelect();

        znodeExec = new ZnodeExec(select.getFromZnode());

        whereFilteringExec = new FilteringExec(select.getWhereCondition());

        groupByExpressions = select.hasGroupByExpression() ? select.getGroupByExpression().toComponents() : null;
        analyzeGrouping();

        havingFilteringExec = new FilteringExec(select.getHavingCondition());

        sortingExec = new SortingExec(select.getOrderByExpression(), columnsNameIndex);

        // endregion
    }

    public ResultSet execute(ZooKeeper zookeeper) throws ZQLException {
        try {
            final var entries = znodeExec.znodesToEntries(zookeeper);
            whereFilteringExec.execute(entries);
            final var rows = expressionExec.entriesToRows(entries);

            if (groupingIndex != null) {
                processGrouping(rows);
            }

            havingFilteringExec.execute(rows, columnsNameIndex);
            sortingExec.execute(rows);

            return new ResultSet(columnsName, columnsNameIndex, rows);
        } catch (Exception e) {
            throw new ZQLException(e);
        }
    }

    private void analyzeSelect() throws ZQLException {
        final int columnsCount = selectExpressions != null ? selectExpressions.length : 0;

        columnsExpressionType = new Expression.Type[columnsCount];
        columnsName = new String[columnsCount];
        columnsNameIndex = new HashMap<>(columnsCount);

        for (int i = 0; i < columnsCount; i++) {
            columnsExpressionType[i] = selectExpressions[i].getType() == Expression.Type.ALIAS ? ((ExpressionWrapper2) selectExpressions[i]).getWrappedExpression1().getType() : selectExpressions[i].getType();

            if (selectExpressions[i].hasAlias()) {
                columnsName[i] = selectExpressions[i].toAlias();
                columnsNameIndex.putIfAbsent(selectExpressions[i].toName(), i);
            } else {
                columnsName[i] = selectExpressions[i].toName();
                columnsNameIndex.putIfAbsent(columnsName[i], i);
            }
        }
    }

    private void analyzeGrouping() {
        final int groupingExpressionsCount = groupByExpressions != null ? groupByExpressions.length : 0;

        if (Arrays.stream(selectExpressions).allMatch(this::isAggregationFunction)) {
            /* Все выражения - функции агрегации */

            if (groupingExpressionsCount > 0) {
                throw new ZQLException("Statement contains group by but all expressions are aggregation functions");
            }

            groupingIndex = new int[0];
        } else if (Arrays.stream(selectExpressions).anyMatch(this::isAggregationFunction)) {
            /* Как минимум одно, но не все выражения - функции агрегации */

            if (groupByExpressions == null) {
                throw new ZQLException("Statement contains aggregation function but not contains group by");
            }

            /* Подготовить имена выражений и индекс */
            final String[] gropingExpressionsName = new String[groupingExpressionsCount];
            final Map<String, Integer> groupingExpressionsNameIndex = new HashMap<>(groupingExpressionsCount);

            for (int i = 0; i < groupingExpressionsCount; i++) {
                gropingExpressionsName[i] = groupByExpressions[i].toName();
                groupingExpressionsNameIndex.putIfAbsent(gropingExpressionsName[i], i);
            }

            groupingIndex = new int[groupingExpressionsCount];

            /* Проверить что все выражения из group by присутствуют в select */
            for (int i = 0; i < groupingExpressionsCount; i++) {
                final int index = columnsNameIndex.getOrDefault(gropingExpressionsName[i], -1);

                if (index == -1) {
                    throw new ZQLException("Statement contains %s expression in grouping but not contains that expression in select".formatted(gropingExpressionsName[i]));
                }

                /* Отметить поле для группировки */
                groupingIndex[i] = index;
            }

            /* Проверить что все выражения, кроме функций агрегации, в select присутствуют в group by */
            for (int i = 0; i < columnsName.length; i++) {
                if (isAggregationFunction(selectExpressions[i])) {
                    continue;
                }

                final int index = groupingExpressionsNameIndex.getOrDefault(columnsName[i], -1);

                if (index == -1) {
                    throw new ZQLException("Statement contains %s expression in select but not contains that expression in grouping".formatted(columnsName[i]));
                }
            }
        } else {
            /* Все выражения - не функции агрегации */

            groupingIndex = null;
        }
    }

    private boolean isAggregationFunction(Expression expression) {
        return expression.getType() == Expression.Type.COUNT
                || expression.getType() == Expression.Type.SUM
                || expression.getType() == Expression.Type.AVG
                || expression.getType() == Expression.Type.MIN
                || expression.getType() == Expression.Type.MAX
                || expression.getType() == Expression.Type.ALIAS && ((ExpressionWrapper2) expression).getWrappedExpression1().getType() == Expression.Type.COUNT
                || expression.getType() == Expression.Type.ALIAS && ((ExpressionWrapper2) expression).getWrappedExpression1().getType() == Expression.Type.SUM
                || expression.getType() == Expression.Type.ALIAS && ((ExpressionWrapper2) expression).getWrappedExpression1().getType() == Expression.Type.AVG
                || expression.getType() == Expression.Type.ALIAS && ((ExpressionWrapper2) expression).getWrappedExpression1().getType() == Expression.Type.MIN
                || expression.getType() == Expression.Type.ALIAS && ((ExpressionWrapper2) expression).getWrappedExpression1().getType() == Expression.Type.MAX;
    }

    private void processGrouping(List<Object[]> rows) {
        @NoArgsConstructor
        class Wrapper {
            private Object[] row;
            private int consumed = 0;

            public Wrapper(Object[] row) {
                this.row = row;
            }

            public void consume(Wrapper another) {
                final Object[] anotherRow = another.row;

                for (int i = 0; i < row.length; i++) {
                    switch (columnsExpressionType[i]) {
                        case COUNT -> {
                            if (row[i] instanceof Integer value) {
                                row[i] = value + 1;
                            } else {
                                // todo exception
                            }
                        }
                        case SUM -> {
                            if (row[i] instanceof Integer value1) {
                                if (anotherRow[i] instanceof Integer value2) {
                                    row[i] = value1 + value2;
                                } else if (anotherRow[i] instanceof Float value2) {
                                    row[i] = value1 + value2;
                                } else {
                                    // todo exception
                                }
                            } else if (row[i] instanceof Float value1) {
                                if (anotherRow[i] instanceof Integer value2) {
                                    row[i] = value1 + value2;
                                } else if (anotherRow[i] instanceof Float value2) {
                                    row[i] = value1 + value2;
                                } else {
                                    // todo exception
                                }
                            } else {
                                // todo exception
                            }
                        }
                        case AVG -> {
                            if (row[i] instanceof Integer value1) {
                                if (anotherRow[i] instanceof Integer value2) {
                                    row[i] = value1 + (value2 - value1) / (consumed + 1);
                                } else if (anotherRow[i] instanceof Float value2) {
                                    row[i] = value1 + (value2 - value1) / (consumed + 1);
                                } else {
                                    // todo exception
                                }
                            } else if (row[i] instanceof Float value1) {
                                if (anotherRow[i] instanceof Integer value2) {
                                    row[i] = value1 + (value2 - value1) / (consumed + 1);
                                } else if (anotherRow[i] instanceof Float value2) {
                                    row[i] = value1 + (value2 - value1) / (consumed + 1);
                                } else {
                                    // todo exception
                                }
                            } else {
                                // todo exception
                            }
                        }
                        case MIN -> {
                            if (row[i] instanceof Integer value1) {
                                if (anotherRow[i] instanceof Integer value2) {
                                    row[i] = Math.min(value1, value2);
                                } else if (anotherRow[i] instanceof Float value2) {
                                    row[i] = Math.min(value1, value2);
                                } else {
                                    // todo exception
                                }
                            } else if (row[i] instanceof Float value1) {
                                if (anotherRow[i] instanceof Integer value2) {
                                    row[i] = Math.min(value1, value2);
                                } else if (anotherRow[i] instanceof Float value2) {
                                    row[i] = Math.min(value1, value2);
                                } else {
                                    // todo exception
                                }
                            } else {
                                // todo exception
                            }
                        }
                        case MAX -> {
                            if (row[i] instanceof Integer value1) {
                                if (anotherRow[i] instanceof Integer value2) {
                                    row[i] = Math.max(value1, value2);
                                } else if (anotherRow[i] instanceof Float value2) {
                                    row[i] = Math.max(value1, value2);
                                } else {
                                    // todo exception
                                }
                            } else if (row[i] instanceof Float value1) {
                                if (anotherRow[i] instanceof Integer value2) {
                                    row[i] = Math.max(value1, value2);
                                } else if (anotherRow[i] instanceof Float value2) {
                                    row[i] = Math.max(value1, value2);
                                } else {
                                    // todo exception
                                }
                            } else {
                                // todo exception
                            }
                        }
                    }
                }

                consumed++;
            }

            @Override
            public int hashCode() {
                return IntStream.range(0, groupingIndex.length).map(i -> row[i].hashCode()).sum();
            }

            @Override
            public boolean equals(Object object) {
                return object instanceof Wrapper wrapper && IntStream.range(0, groupingIndex.length).allMatch(i -> Objects.equals(row[i], wrapper.row[i]));
            }
        }

        final Map<Wrapper, Wrapper> groups = new HashMap<>();
        final Wrapper current = new Wrapper();

        rows.removeIf(row -> {
            current.row = row;

            Wrapper buff = groups.get(current);
            if (buff != null) {
                buff.consume(current);
                return true;
            } else {
                buff = new Wrapper(current.row);
                groups.put(buff, buff);
                return false;
            }
        });
    }

}
