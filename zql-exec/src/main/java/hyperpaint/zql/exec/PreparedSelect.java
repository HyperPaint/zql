package hyperpaint.zql.exec;

import hyperpaint.zql.lang.ZQL;
import hyperpaint.zql.lang.ZQLException;
import hyperpaint.zql.lang.condition.Condition;
import hyperpaint.zql.lang.expression.Expression;
import hyperpaint.zql.lang.expression.ExpressionCollection;
import hyperpaint.zql.lang.statement.Select;
import hyperpaint.zql.lang.znode.Znode;
import hyperpaint.zql.lang.znode.ZnodeCollection;
import hyperpaint.zql.lang.znode.ZnodePath;
import hyperpaint.zql.lang.znode.ZnodeWrapper;
import lombok.NonNull;
import org.apache.zookeeper.ZooKeeper;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class PreparedSelect {
    private final ZooKeeper connection;
    private final String query;

    private Select select;

    PreparedSelect(@NonNull ZooKeeper connection, @NonNull String query) {
        this.connection = connection;
        this.query = query;
    }

    public ResultSet executeQuery() throws ZQLException {
        if (select == null) {
            select = (Select) ZQL.parse(query);
        }


        final Map<String, Integer> columns = new HashMap<>();

        if (select.hasSelectExpression()) {
            final List<String> buff = new ArrayList<>();

            processSelectColumns(buff, select.getSelectExpression());

            for (int i = 0; i < buff.size(); i++) {
                if (columns.containsKey(buff.get(i))) {
                    throw new ZQLException("Query contains duplicate column names or aliases: " + buff.get(i));
                }

                columns.put(buff.get(i), i);
            }

            // todo debug
            System.out.println("columns:");
            for (var item : columns.entrySet()) {
                System.out.println(item.getKey() + " -> " + item.getValue());
            }
        }

        final Map<String, String> znodes = new HashMap<>();

        if (select.hasFromZnode()) {
            processFromZnodes(znodes, select.getFromZnode());

            // todo debug
            System.out.println("znodes:");
            for (var item : znodes.entrySet()) {
                System.out.println(item.getKey() + " -> " + item.getValue());
            }
        }

        if (select.hasWhereCondition()) {
            processWhereFilter(znodes, select.getWhereCondition());

            // todo debug
            System.out.println("filter:");
            for (var item : znodes.entrySet()) {
                System.out.println(item.getKey() + " -> " + item.getValue());
            }
        }

        final List<Object[]> rows = new ArrayList<>();

//        collectRows(resultSet);
//        groupRows(resultSet);

        return new ResultSet(columns, rows);
    }

    private void processSelectColumns(List<String> result, Expression expression) throws IllegalArgumentException {
        switch (expression.getType()) {
            case COMMA -> {
                final ExpressionCollection expressionCollection = (ExpressionCollection) expression;

                for (var item : expressionCollection.getList()) {
                    processSelectColumns(result, item);
                }
            }
            case ALIAS, COUNT, SUM, AVG, MIN, MAX, JSON_PATH, TEXT, NUMBER, IDENTIFIER -> result.add(expression.name());
            // case ORDER -> throw new IllegalArgumentException("Unexpected value: " + expression.getType());
            default -> throw new IllegalArgumentException("Unexpected value: " + expression.getType());
        }
    }

    private void processFromZnodes(Map<String, String> result, Znode znode) throws IllegalArgumentException, ZQLException {
        switch (znode.getType()) {
            case COMMA -> {
                final ZnodeCollection znodeCollection = (ZnodeCollection) znode;

                for (var item : znodeCollection.getList()) {
                    processFromZnodes(result, item);
                }
            }
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
                                    result.put(item, data);
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
                        result.put(path, data);
                    }
                } catch (Exception e) {
                    throw new ZQLException(e);
                }
            }
            default -> throw new IllegalArgumentException("Unexpected value: " + znode.getType());
        }
    }

    private void processWhereFilter(Map<String, String> result, Condition condition) throws ZQLException {
        try {
            result.entrySet().removeIf(entry -> !condition.pass(entry.getKey(), entry.getValue()));
        } catch (Exception e) {
            throw new ZQLException(e);
        }
    }

//
//    private void analyzeAggregation(Expression expression) {
//        switch (expression.getType()) {
//            case COMMA -> {
//                final ExpressionCollection expressionComma = (ExpressionCollection) expression;
//                expressionComma.getCollection().forEach(this::analyzeAggregation);
//            }
//            case ALIAS -> {
//                final ExpressionAlias selectExpressionAlias = (ExpressionAlias) expression;
//                analyzeAggregation(expressionAlias.getWrappedExpression());
//            }
//            case COUNT -> columnsAggregateFunctions.add(Expression.Type.COUNT);
//            case SUM -> columnsAggregateFunctions.add(Expression.Type.SUM);
//            case AVG -> columnsAggregateFunctions.add(Expression.Type.AVG);
//            case MIN -> columnsAggregateFunctions.add(Expression.Type.MIN);
//            case MAX -> columnsAggregateFunctions.add(Expression.Type.MAX);
//            case JSON -> columnsAggregateFunctions.add(Expression.Type.JSON);
//            case PATH -> columnsAggregateFunctions.add(Expression.Type.PATH);
//            case DATA -> columnsAggregateFunctions.add(Expression.Type.DATA);
//            case TEXT -> columnsAggregateFunctions.add(Expression.Type.TEXT);
//            case NUMBER -> columnsAggregateFunctions.add(Expression.Type.NUMBER);
//            default -> throw new ZQLException("Unhandled expression type; expression=" + expression);
//        }
//    }
//


//
//    private void collectRows(ResultSet resultSet) {
//        if (select.getExpressions() == null) {
//            return;
//        }
//
//        try {
//            resultSet.znodes.forEach((key, value) -> {
//                final List<String> row = new ArrayList<>();
//                collectRows(select.getExpressions(), row, key, value);
//                resultSet.rows.add(row);
//            });
//        } catch (Exception e) {
//            throw new ZQLException("Rows collecting error; query=" + query, e);
//        }
//    }
//
//    private void collectRows(Expression expression, List<String> row, String path, String data) {
//        switch (expression.getType()) {
//            case COMMA -> {
//                final ExpressionCollection expressionComma = (ExpressionCollection) expression;
//                expressionComma.getCollection().forEach(e -> collectRows(e, row, path, data));
//            }
//            case ALIAS -> {
//                final ExpressionAlias selectExpressionAlias = (ExpressionAlias) expression;
//                collectRows(expressionAlias.getWrappedExpression(), row, path, data);
//            }
//            case COUNT, SUM, AVG, MIN, MAX -> {
//                final ExpressionWrapper expressionWrapper = (ExpressionWrapper) expression;
//                collectRows(expressionWrapper.getWrappedExpression(), row, path, data);
//            }
////            case JSON -> {
////                final Expression expressionJson = (Expression) expression;
////                row.add(expressionJson.getText());
////            }
//            case PATH -> row.add(path);
//            case DATA -> row.add(data);
//            case TEXT, NUMBER -> {
//                final ExpressionString expressionString = (ExpressionString) expression;
//                row.add(expressionString.getText());
//            }
//            default -> throw new ZQLException("Unhandled expression type; expression=" + expression);
//        }
//    }
//
//    private void groupRows(ResultSet resultSet) {
//        if (select.getGroups() == null && !isAllColumnsAggregateFunctions) {
//            return;
//        }
//
//        try {
//            final Map<String, List<String>> uniqueGroups = new HashMap<>();
//            final int[] counter = {0};
//            resultSet.rows.removeIf(row -> {
//                final StringBuilder stringBuilder = new StringBuilder();
//
//                for (int i = 0; i < row.size(); i++) {
//                    final Expression.Type type = columnsAggregateFunctions.get(i);
//                    switch (type) {
//                        case COUNT, SUM, AVG, MIN, MAX -> {
//                            // ...
//                        }
//                        case JSON, PATH, DATA, TEXT, NUMBER -> {
//                            stringBuilder.append(row.get(i));
//                        }
//                        default -> throw new ZQLException("Unhandled expression type; type=" + type);
//                    }
//                }
//
//                final String group = stringBuilder.toString();
//                final List<String> groupRow = uniqueGroups.get(group);
//
//                boolean result = false;
//
//                if (groupRow == null) {
//                    uniqueGroups.put(group, row);
//                } else {
//                    for (int i = 0; i < groupRow.size(); i++) {
//                        final Expression.Type type = columnsAggregateFunctions.get(i);
//                        switch (type) {
//                            case COUNT -> {
//                                try {
//                                    final int buff = Integer.parseInt(groupRow.get(i)) + 1;
//                                    groupRow.set(i, String.valueOf(buff));
//                                } catch (NumberFormatException e) {
//                                    // ...
//                                } finally {
//                                    result = true;
//                                }
//                            }
//                            case SUM -> {
//                                try {
//                                    final float buff = Float.parseFloat(groupRow.get(i)) + Float.parseFloat(row.get(i));
//                                    groupRow.set(i, String.valueOf(buff));
//                                } catch (NumberFormatException e) {
//                                    // ...
//                                } finally {
//                                    result = true;
//                                }
//                            }
//                            case AVG -> {
//                                try {
//                                    final float groupRowValue = Float.parseFloat(groupRow.get(i));
//                                    final float value = Float.parseFloat(row.get(i));
//                                    groupRow.set(i, String.valueOf( groupRowValue + (value - groupRowValue) / (counter[0] + 1)) );
//                                } catch (NumberFormatException e) {
//                                    // ...
//                                } finally {
//                                    result = true;
//                                }
//                            }
//                            case MIN -> {
//                                try {
//                                    final float buff = Math.min(Float.parseFloat(groupRow.get(i)), Float.parseFloat(row.get(i)));
//                                    groupRow.set(i, String.valueOf(buff));
//                                } catch (NumberFormatException e) {
//                                    // ...
//                                } finally {
//                                    result = true;
//                                }
//                            }
//                            case MAX -> {
//                                try {
//                                    final float buff = Math.max(Float.parseFloat(groupRow.get(i)), Float.parseFloat(row.get(i)));
//                                    groupRow.set(i, String.valueOf(buff));
//                                } catch (NumberFormatException e) {
//                                    // ...
//                                } finally {
//                                    result = true;
//                                }
//                            }
//                            case JSON, PATH, DATA, TEXT, NUMBER -> {
//                                // ...
//                            }
//                            default -> throw new ZQLException("Unhandled expression type; type=" + type);
//                        }
//                    }
//                }
//
//                counter[0]++;
//                return result;
//            });
//        } catch (Exception e) {
//            throw new ZQLException("Rows grouping error; query=" + query, e);
//        }
//    }
}
