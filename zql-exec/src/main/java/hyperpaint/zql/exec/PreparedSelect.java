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
import lombok.NonNull;
import org.apache.zookeeper.ZooKeeper;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class PreparedSelect {
    private final ZooKeeper connection;
    private final String query;

    private boolean analyzed = false;

    private Expression[] selectExpressions;
    private ResultSetHeader header;

    private Znode[] fromZnodes;

    private Condition whereCondition;

    private Expression[] groupByExpressions;
    private boolean[] groups;

    private Condition havingCondition;

    private Expression[] orderByExpressions;

    PreparedSelect(@NonNull ZooKeeper connection, @NonNull String query) {
        this.connection = connection;
        this.query = query;
    }

    public ResultSet executeQuery() throws ZQLException {
        if (!analyzed) {
            Select select = (Select) ZQL.parse(query);

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

            if (select.hasSelectExpression()) {
                selectExpressions = select.getSelectExpression().toComponents();
                buildResultSetHeader();
            }

            if (select.hasFromZnode()) {
                fromZnodes = select.getFromZnode().toComponents();
            }

            whereCondition = select.getWhereCondition();

            if (select.hasGroupByExpression()) {
                groupByExpressions = select.getGroupByExpression().toComponents();
            }

            analyzeGrouping();

            havingCondition = select.getHavingCondition();

            if (select.hasOrderByExpression()) {
                orderByExpressions = select.getOrderByExpression().toComponents();
                analyzeSorting();
            }

            // endregion

            analyzed = true;
        }

        if (selectExpressions == null) {
            return ping();
        }

        final Map<String, String> entries = new HashMap<>();

        if (fromZnodes != null) {
            processZnodes(entries);
        }

        if (whereCondition != null) {
            processFiltering(entries);
        }

        final List<Object[]> rows = new ArrayList<>(entries.size());
        processEntriesToRows(entries, rows);

        if (groups != null) {
            processGrouping(rows);
        }

        if (havingCondition != null) {
            processFiltering(rows);
        }

        if (orderByExpressions != null) {

        }

        return new ResultSet(header, rows);
    }

    private ResultSet ping() {
        try {
            connection.exists("/", false);
        } catch (Exception e) {
            throw new ZQLException(e);
        }

        return ResultSet.EMPTY;
    }

    private void buildResultSetHeader() throws ZQLException {
        final String[] columns = new String[selectExpressions.length];
        final Map<String, Integer> index = new HashMap<>(selectExpressions.length);

        for (int i = 0; i < selectExpressions.length; i++) {
            if (selectExpressions[i].hasAlias()) {
                columns[i] = selectExpressions[i].toAlias();
                index.putIfAbsent(selectExpressions[i].toName(), i);
            } else {
                columns[i] = selectExpressions[i].toName();
                index.putIfAbsent(columns[i], i);
            }
        }

        header = new ResultSetHeader(columns, index);
    }

    private void analyzeGrouping() {
        if (Arrays.stream(selectExpressions).allMatch(this::isAggregationFunction)) {
            if (groupByExpressions != null) {
                throw new ZQLException("Statement contains group by but all expressions are aggregation functions");
            }

            groups = new boolean[selectExpressions.length];

            for (int i = 0; i < selectExpressions.length; i++) {
                groups[i] = false;
            }
        } else if (Arrays.stream(selectExpressions).anyMatch(this::isAggregationFunction)) {
            if (groupByExpressions == null) {
                throw new ZQLException("Statement not contains group by but contains aggregation functions");
            }

            final Map<String, Integer> groupsIndex = new HashMap<>(groupByExpressions.length);

            for (int i = 0; i < groupByExpressions.length; i++) {
                groupsIndex.putIfAbsent(groupByExpressions[i].toName(), i);
            }

            groups = new boolean[selectExpressions.length];

            for (int i = 0; i < selectExpressions.length; i++) {
                final String name = selectExpressions[i].toName();
                final int index = groupsIndex.get(name);

                if (!isAggregationFunction(selectExpressions[i]) && index == -1) {
                    throw new ZQLException("Statement contains aggregation expression but not contains that expression in group by: " + name);
                }

                groups[i] = true;
            }

            for (int i = 0; i < groupByExpressions.length; i++) {
                final String name = groupByExpressions[i].toName();
                final int index = header.get(name);

                if (index == -1) {
                    throw new ZQLException("Statement contains group by expression but not contains that expression in select: " + name);
                }

                groups[index] = true;
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
            rows.removeIf(row -> !havingCondition.toValue(row, header.getColumnsIndex()));
        } catch (Exception e) {
            throw new ZQLException(e);
        }
    }

    private void processGrouping(List<Object[]> rows) {
        final Map<String, Object[]> uniqueRows = new HashMap<>();

        rows.removeIf(row -> {
            final StringBuilder groupBuilder = new StringBuilder();

            for (int i = 0; i < row.length; i++) {
                if (groups[i]) {
                    groupBuilder.append(row[i]);
                }
            }

            final String group = groupBuilder.toString();
            final Object[] uniqueRow = uniqueRows.get(group);

            if (uniqueRow == null) {
                uniqueRows.put(group, row);

                return false;
            } else {
                for (int i = 0; i < row.length; i++) {
                    switch (selectExpressions[i].getType()) {
                        case COUNT, SUM, AVG, MIN, MAX -> {
                            if (uniqueRow[i] instanceof Integer) {
                                uniqueRow[i] = ((int) uniqueRow[i]) + ((int) row[i]);
                            } else if (uniqueRow[i] instanceof Float) {
                                uniqueRow[i] = ((float) uniqueRow[i]) + ((float) row[i]);
                            } else {
                                throw new IllegalArgumentException("Unexpected value: " + selectExpressions[i].getType());
                            }
                        }
                    }
                }

                return true;
            }
        });
    }

    private void processSorting(List<Object[]> rows) {

    }
}
