package hyperpaint.zql.exec;

import hyperpaint.zql.lang.ZQLException;
import hyperpaint.zql.lang.condition.Condition;
import hyperpaint.zql.lang.expression.Expression;
import hyperpaint.zql.lang.expression.ExpressionWrapper;
import hyperpaint.zql.lang.expression.ExpressionWrapper2;
import hyperpaint.zql.lang.statement.Select;
import hyperpaint.zql.lang.znode.Znode;
import hyperpaint.zql.lang.znode.ZnodePath;
import hyperpaint.zql.lang.znode.ZnodeWrapper;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.zookeeper.ZooKeeper;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.IntStream;

public class SelectStatement implements Statement {
    private final Expression[] selectExpressions;
    private final Znode[] fromZnodes;
    private final Condition whereCondition;
    private final Expression[] groupByExpressions;
    private final Condition havingCondition;
    private final Expression[] orderByExpressions;

    /** Наименования полей */
    private String[] columnsName;
    /** Индексы наименований полей */
    private Map<String, Integer> columnsNameIndex;

    /** Индексы группировки полей */
    private int[] groupingIndex;
    /** Типы выражений в полях */
    private Expression.Type[] columnsType;

    /** Компаратор для сортировки */
    private Comparator<Object[]> sortingComparator;

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
        analyzeSelect();

        fromZnodes = select.hasFromZnode() ? select.getFromZnode().toComponents() : null;

        whereCondition = select.getWhereCondition();

        groupByExpressions = select.hasGroupByExpression() ? select.getGroupByExpression().toComponents() : null;
        analyzeGrouping();

        havingCondition = select.getHavingCondition();

        orderByExpressions = select.hasOrderByExpression() ? select.getOrderByExpression().toComponents() : null;
        analyzeSorting();

        // endregion
    }

    public ResultSet execute(ZooKeeper connection) throws ZQLException {
        final Map<String, String> entries = fromZnodes != null ? processZnodes(connection) : new HashMap<>();
        final List<Object[]> rows = processEntriesToRows(entries);

        if (whereCondition != null) processFiltering(rows, whereCondition);
        if (groupingIndex != null) processGrouping(rows);
        if (havingCondition != null) processFiltering(rows, havingCondition);
        if (sortingComparator != null) processSorting(rows);


        return new ResultSet(columnsName, columnsNameIndex, rows);
    }

    private void analyzeSelect() throws ZQLException {
        final int columnsCount = selectExpressions != null ? selectExpressions.length : 0;

        columnsType = new Expression.Type[columnsCount];
        columnsName = new String[columnsCount];
        columnsNameIndex = new HashMap<>(columnsCount);

        for (int i = 0; i < columnsCount; i++) {
            columnsType[i] = selectExpressions[i].getType() == Expression.Type.ALIAS ? ((ExpressionWrapper2) selectExpressions[i]).getWrappedExpression1().getType() : selectExpressions[i].getType();

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

    private void analyzeSorting() {
        final int sortingColumnsLength = orderByExpressions != null ? orderByExpressions.length : 0;

        for (int i = 0; i < sortingColumnsLength; i++) {
            final Comparator<Object[]> buff;

            switch (orderByExpressions[i].getType()) {
                case ORDER_BY_ASC -> {
                    final ExpressionWrapper expressionWrapper = (ExpressionWrapper) orderByExpressions[i];
                    final int index = columnsNameIndex.getOrDefault(expressionWrapper.getWrappedExpression().toName(), -1);

                    if (index == -1) {
                        throw new ZQLException("order by contains non-exist expression");
                    }

                    buff = analyzeSortingGetComparator(index);
                }
                case ORDER_BY_DESC -> {
                    final ExpressionWrapper expressionWrapper = (ExpressionWrapper) orderByExpressions[i];
                    final int index = columnsNameIndex.getOrDefault(expressionWrapper.getWrappedExpression().toName(), -1);

                    if (index == -1) {
                        throw new ZQLException("order by contains non-exist expression");
                    }

                    buff = analyzeSortingGetComparator(index).reversed();
                }
                default -> throw new ZQLException("order by incorrect");
            }

            sortingComparator = sortingComparator != null ? sortingComparator.thenComparing(buff) : buff;
        }
    }

    private Comparator<Object[]> analyzeSortingGetComparator(int index) {
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

    private Map<String, String> processZnodes(ZooKeeper connection) throws IllegalArgumentException, ZQLException {
        final Map<String, String> entries = new HashMap<>();

        for (var znode : fromZnodes) {
            switch (znode.getType()) {
                case LIST -> {
                    final ZnodeWrapper znodeWrapper = (ZnodeWrapper) znode;

                    try {
                        /* Получить изначальный путь и количество ls */

                        final String path;

                        int ls = 1;
                        Znode wrappedZnode = znodeWrapper.getWrappedZnode();

                        main:
                        while (true) {
                            switch (wrappedZnode.getType()) {
                                case LIST -> {
                                    ls++;
                                    wrappedZnode = ((ZnodeWrapper) wrappedZnode).getWrappedZnode();
                                }
                                case PATH -> {
                                    path = ((ZnodePath) wrappedZnode).getPath();
                                    break main;
                                }
                                default -> throw new IllegalArgumentException("Unexpected value: " + wrappedZnode.getType());
                            }
                        }

                        /* Получить znode вместе с данными */

                        List<String> prev, next = null;

                        for (int i = 0; i < ls; i++) {
                            /* Корневой znode */
                            if (i == 0) {
                                next = connection.getChildren(path, null)
                                        .stream()
                                        .map(s -> path.equals("/") ? path + s : path + "/" + s)
                                        .toList();
                            }

                            /* Промежуточные znode */
                            if (i != ls - 1) {
                                prev = next;
                                next = new ArrayList<>();

                                for (var item : prev) {
                                    next.addAll(
                                            connection.getChildren(item, null)
                                                    .stream()
                                                    .map(s -> item.equals("/") ? item + s : item + "/" + s)
                                                    .toList()
                                    );
                                }
                            }

                            /* Необходимая глубина znode */
                            if (i == ls - 1) {
                                prev = next;
                                for (var item : prev) {
                                    final boolean exists = connection.exists(item, null) != null;

                                    if (exists) {
                                        final byte[] bytes = connection.getData(item, null, null);
                                        final String data = bytes != null ? new String(bytes, StandardCharsets.UTF_8) : null;
                                        entries.put(item, data);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        throw new ZQLException(e);
                    }
                }
                case PATH -> {
                    final ZnodePath znodePath = (ZnodePath) znode;

                    try {
                        final String path = znodePath.getPath();
                        final boolean exists = connection.exists(path, null) != null;

                        if (exists) {
                            final byte[] bytes = connection.getData(path, null, null);
                            final String data = bytes != null ? new String(bytes, StandardCharsets.UTF_8) : null;
                            entries.put(path, data);
                        }
                    } catch (Exception e) {
                        throw new ZQLException(e);
                    }
                }
                default -> throw new IllegalArgumentException("Unexpected value: " + znode.getType());
            }
        }

        return entries;
    }

    private List<Object[]> processEntriesToRows(Map<String, String> entries) {
        final List<Object[]> rows = new ArrayList<>();

        for (var znode : entries.entrySet()) {
            final Object[] row = new Object[columnsName.length];

            for (int i = 0; i < columnsName.length; i++) {
                row[i] = selectExpressions[i].toValue(znode.getKey(), znode.getValue());
            }

            rows.add(row);
        }

        return rows;
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
                    switch (columnsType[i]) {
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

    private void processFiltering(List<Object[]> rows, Condition condition) throws ZQLException {
        try {
            rows.removeIf(row -> !condition.toValue(row, columnsNameIndex));
        } catch (Exception e) {
            throw new ZQLException(e);
        }
    }

    private void processSorting(List<Object[]> rows) {
        rows.sort(sortingComparator);
    }
}
