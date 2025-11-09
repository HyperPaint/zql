package hyperpaint.zql.exec;

import hyperpaint.zql.lang.ZQL;
import hyperpaint.zql.lang.ZQLException;
import hyperpaint.zql.lang.condition.Condition;
import hyperpaint.zql.lang.expression.Expression;
import hyperpaint.zql.lang.expression.ExpressionWrapper2;
import hyperpaint.zql.lang.statement.Select;
import hyperpaint.zql.lang.znode.Znode;
import hyperpaint.zql.lang.znode.ZnodePath;
import hyperpaint.zql.lang.znode.ZnodeWrapper;
import lombok.*;
import org.apache.zookeeper.ZooKeeper;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class PreparedSelect {
    private final ZooKeeper connection;

    private final Expression[] selectExpressions;
    /** Наименования полей */
    private String[] columnsName;
    /** Индекс для быстрого доступа к индексу наименования поля по его наименованию */
    private Map<String, Integer> columnsNameIndex;

    private final Znode[] fromZnodes;

    private final Condition whereCondition;

    private final Expression[] groupByExpressions;
    /** Если для индекса проставлено true, значит каждое уникальное поле с этим индексом образует отдельную группу */
    private boolean[] columnsGroupingMark;

    private final Condition havingCondition;

    private final Expression[] orderByExpressions;

    PreparedSelect(@NonNull ZooKeeper connection, @NonNull String query) {
        this.connection = connection;

        final Select select = (Select) ZQL.parse(query);

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

        // region other

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

    public ResultSet executeQuery() throws ZQLException {
        final Map<String, String> entries = new HashMap<>();

        if (fromZnodes != null) {
            processZnodes(entries);
        }

        if (whereCondition != null) {
            processFiltering(entries);
        }

        final List<Object[]> rows = new ArrayList<>(entries.size());
        processEntriesToRows(entries, rows);

        if (columnsGroupingMark != null) {
            processGrouping(rows);
        }

        if (havingCondition != null) {
            processFiltering(rows);
        }

        if (orderByExpressions != null) {

        }

        return new ResultSet(columnsName, columnsNameIndex, rows);
    }

    private void analyzeSelect() throws ZQLException {
        final int size = selectExpressions != null ? selectExpressions.length : 0;

        columnsName = new String[size];
        columnsNameIndex = new HashMap<>(size);

        for (int i = 0; i < size; i++) {
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
        if (Arrays.stream(selectExpressions).allMatch(this::isAggregationFunction)) {
            /* Все выражения - функции агрегации */

            if (groupByExpressions != null) throw new ZQLException("Statement contains group by but all expressions are aggregation functions");

            columnsGroupingMark = new boolean[selectExpressions.length];
            Arrays.fill(columnsGroupingMark, false);
        } else if (Arrays.stream(selectExpressions).anyMatch(this::isAggregationFunction)) {
            /* Одно из выражений - функция агрегации */

            if (groupByExpressions == null) throw new ZQLException("Statement not contains group by but contains aggregation functions");

            columnsGroupingMark = new boolean[selectExpressions.length];
            Arrays.fill(columnsGroupingMark, false);

            final Map<String, Integer> groupsIndex = new HashMap<>(groupByExpressions.length);
            for (int i = 0; i < groupByExpressions.length; i++) {
                groupsIndex.putIfAbsent(groupByExpressions[i].toName(), i);
            }

            /* Если для выражения в select найдено выражение в group by, отметить поле для группировки */
            for (int i = 0; i < selectExpressions.length; i++) {
                final int index = groupsIndex.get(columnsName[i]);

                /* Если выражение в select не функция агрегации и отсутствует в group by, выбросить исключение */
                if (!isAggregationFunction(selectExpressions[i]) && index == -1) {
                    throw new ZQLException("Statement contains contains expression in group by: " + columnsName[i]);
                }

                columnsGroupingMark[i] = true;
            }

            /* Если для выражения в group by найдено выражение в select, отметить поле для группировки */
            for (Expression item : groupByExpressions) {
                final int index = columnsNameIndex.get(item.toName());

                /* Если выражение в group by отсутствует в select, выбросить исключение */
                if (index == -1) {
                    throw new ZQLException("Statement contains group by expression but not contains that expression in select: " + item.toName());
                }

                columnsGroupingMark[index] = true;
            }
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

    }

    private void processZnodes(Map<String, String> entries) throws IllegalArgumentException, ZQLException {
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
    }

    private void processFiltering(Map<String, String> entries) throws ZQLException {
        try {
            entries.entrySet().removeIf(entry -> !whereCondition.toValue(entry.getKey(), entry.getValue()));
        } catch (Exception e) {
            throw new ZQLException(e);
        }
    }

    private void processEntriesToRows(Map<String, String> entries, List<Object[]> rows) {
        for (var znode : entries.entrySet()) {
            final Object[] row = new Object[selectExpressions.length];

            for (int i = 0; i < selectExpressions.length; i++) {
                row[i] = selectExpressions[i].toValue(znode.getKey(), znode.getValue());
            }

            rows.add(row);
        }
    }

    private void processFiltering(List<Object[]> rows) throws ZQLException {
        try {
            rows.removeIf(row -> !havingCondition.toValue(row, columnsNameIndex));
        } catch (Exception e) {
            throw new ZQLException(e);
        }
    }

    private void processGrouping(List<Object[]> rows) {
        @Setter
        class RowWrapper {
            private @Getter Object[] row;
            private final boolean[] marks;

            public RowWrapper(boolean[] marks) {
                this.marks = marks;
            }

            public RowWrapper(RowWrapper another) {
                this.row = another.row;
                this.marks = another.marks;
            }

            public void add(RowWrapper another) {
                final Object[] anotherRow = another.getRow();
                for (int i = 0; i < row.length; i++) {
                    switch (selectExpressions[i].getType()) {
                        case COUNT, SUM, AVG, MIN, MAX -> {
                            if (row[i] instanceof Integer) {
                                row[i] = ((int) row[i]) + ((int) anotherRow[i]);
                            } else if (row[i] instanceof Float) {
                                row[i] = ((float) row[i]) + ((float) anotherRow[i]);
                            } else {
                                throw new IllegalArgumentException("Unexpected value: " + selectExpressions[i].getType());
                            }
                        }
                    }
                }
            }

            @Override
            public int hashCode() {
                int result = 0;

                for (int i = 0; i < marks.length; i++) {
                    if (marks[i]) {
                        result += row[i].hashCode();
                    }
                }

                return result;
            }

            @Override
            public boolean equals(Object object) {
                if (object instanceof RowWrapper) {
                    for (int i = 0; i < marks.length; i++) {
                        if (marks[i]) {
                            if (!Objects.equals(row[i], ((RowWrapper) object).row[i])) {
                                return false;
                            }
                        }
                    }

                    return true;
                } else {
                    return false;
                }
            }
        }

        final Map<RowWrapper, RowWrapper> groups = new HashMap<>();
        final RowWrapper currentRowWrapper = new RowWrapper(columnsGroupingMark);

        rows.removeIf(row -> {
            currentRowWrapper.setRow(row);

            RowWrapper uniqueRowWrapper = groups.get(currentRowWrapper);
            if (uniqueRowWrapper != null) {
                uniqueRowWrapper.add(currentRowWrapper);
                return true;
            } else {
                uniqueRowWrapper = new RowWrapper(currentRowWrapper);
                groups.put(uniqueRowWrapper, uniqueRowWrapper);
                return false;
            }
        });
    }

    private void processSorting(List<Object[]> rows) {

    }
}
