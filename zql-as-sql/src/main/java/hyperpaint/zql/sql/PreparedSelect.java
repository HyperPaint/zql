package hyperpaint.zql.sql;

import hyperpaint.zql.lang.ZQLException;
import hyperpaint.zql.lang.ZQLVisitor;
import hyperpaint.zql.lang.antlr4.ZQLLexer;
import hyperpaint.zql.lang.antlr4.ZQLParser;
import hyperpaint.zql.lang.condition.Condition;
import hyperpaint.zql.lang.condition.ConditionCompositeOfExpression;
import hyperpaint.zql.lang.condition.ConditionCompositeOfCondition;
import hyperpaint.zql.lang.condition.ConditionWrapper;
import hyperpaint.zql.expression.*;
import hyperpaint.zql.lang.expression.*;
import hyperpaint.zql.lang.expression.select.ExpressionAlias;
import hyperpaint.zql.lang.statement.Statement;
import hyperpaint.zql.lang.znode.Znode;
import hyperpaint.zql.lang.znode.ZnodePath;
import hyperpaint.zql.lang.znode.ZnodeCollection;
import hyperpaint.zql.lang.znode.ZnodeWrapper;
import lombok.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.curator.framework.CuratorFramework;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class PreparedSelect {
    private @Setter CuratorFramework curator;

    private final String query;
    private Statement statement;

    private final List<String> columns = new ArrayList<>();
    private final List<Expression.Type> columnsAggregateFunctions = new ArrayList<>();
    private boolean isAllColumnsAggregateFunctions = false;

    public PreparedSelect(@NonNull CuratorFramework curator, @NonNull String query) {
        this.curator = curator;
        this.query = query;
    }

    public static class ResultSet {
        private final @Getter List<String> columns;
        private final @Getter List<List<String>> rows;
        private final @Getter Map<String, String> znodes;

        private ResultSet(PreparedSelect preparedStatement) {
            this.columns = preparedStatement.columns;
            this.rows = new ArrayList<>();
            this.znodes = new HashMap<>();
        }
    }

    public ResultSet execute() {
        final ResultSet resultSet = new ResultSet(this);

        if (statement == null) {
            parse();
            analyze();
        }

        if (curator == null) {
            throw new IllegalStateException("Data source is not set");
        }

        collectZnodes(resultSet);
        filterZnodes(resultSet);
        collectRows(resultSet);
        groupRows(resultSet);

        return resultSet;
    }

    private void parse() throws ZQLException {
        try {
            final CharStream charStream = CharStreams.fromString(query);
            final ZQLLexer lexer = new ZQLLexer(charStream);
            final CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            final ZQLParser parser = new ZQLParser(tokenStream);
            final ParseTree parseTree = parser.zql();
            statement = ZQLVisitor.INSTANCE.visit(parseTree);
        } catch (Exception e) {
            throw new ZQLException("Query parsing error; query=" + query, e);
        }
    }

    private void analyze() throws ZQLException {
        if (statement.getExpressions() == null) {
            return;
        }

        try {
            analyzeColumns(statement.getExpressions());
        } catch (Exception e) {
            throw new ZQLException("Analyze columns error; query=" + query, e);
        }

        try {
            analyzeAggregation(statement.getExpressions());
            isAllColumnsAggregateFunctions = columnsAggregateFunctions.stream().allMatch(type -> type == Expression.Type.COUNT || type == Expression.Type.SUM || type == Expression.Type.AVG || type == Expression.Type.MIN || type == Expression.Type.MAX);
        } catch (Exception e) {
            throw new ZQLException("Analyze aggregation error; query=" + query, e);
        }
    }

    private void analyzeColumns(Expression expression) {
        switch (expression.getType()) {
            case COMMA -> {
                final ExpressionCollection expressionComma = (ExpressionCollection) expression;
                expressionComma.getCollection().forEach(this::analyzeColumns);
            }
            case ALIAS -> {
                final ExpressionAlias selectExpressionAlias = (ExpressionAlias) expression;
                columns.add(expressionAlias.getAlias());
            }
            case COUNT -> {
                final ExpressionWrapper expressionWrapper = (ExpressionWrapper) expression;
                final ExpressionString wrappedExpression = (ExpressionString) expressionWrapper.getWrappedExpression();
                columns.add("count(" + wrappedExpression.getText() + ")");
            }
            case SUM -> {
                final ExpressionWrapper expressionWrapper = (ExpressionWrapper) expression;
                final ExpressionString wrappedExpression = (ExpressionString) expressionWrapper.getWrappedExpression();
                columns.add("sum(" + wrappedExpression.getText() + ")");
            }
            case AVG -> {
                final ExpressionWrapper expressionWrapper = (ExpressionWrapper) expression;
                final ExpressionString wrappedExpression = (ExpressionString) expressionWrapper.getWrappedExpression();
                columns.add("avg(" + wrappedExpression.getText() + ")");
            }
            case MIN -> {
                final ExpressionWrapper expressionWrapper = (ExpressionWrapper) expression;
                final ExpressionString wrappedExpression = (ExpressionString) expressionWrapper.getWrappedExpression();
                columns.add("min(" + wrappedExpression.getText() + ")");
            }
            case MAX -> {
                final ExpressionWrapper expressionWrapper = (ExpressionWrapper) expression;
                final ExpressionString wrappedExpression = (ExpressionString) expressionWrapper.getWrappedExpression();
                columns.add("max(" + wrappedExpression.getText() + ")");
            }
            case JSON -> columns.add("json");
            case PATH -> columns.add("path");
            case DATA -> columns.add("data");
            case TEXT -> columns.add("text");
            case NUMBER -> columns.add("number");
            default -> throw new ZQLException("Unhandled expression type; expression=" + expression);
        }
    }

    private void analyzeAggregation(Expression expression) {
        switch (expression.getType()) {
            case COMMA -> {
                final ExpressionCollection expressionComma = (ExpressionCollection) expression;
                expressionComma.getCollection().forEach(this::analyzeAggregation);
            }
            case ALIAS -> {
                final ExpressionAlias selectExpressionAlias = (ExpressionAlias) expression;
                analyzeAggregation(expressionAlias.getWrappedExpression());
            }
            case COUNT -> columnsAggregateFunctions.add(Expression.Type.COUNT);
            case SUM -> columnsAggregateFunctions.add(Expression.Type.SUM);
            case AVG -> columnsAggregateFunctions.add(Expression.Type.AVG);
            case MIN -> columnsAggregateFunctions.add(Expression.Type.MIN);
            case MAX -> columnsAggregateFunctions.add(Expression.Type.MAX);
            case JSON -> columnsAggregateFunctions.add(Expression.Type.JSON);
            case PATH -> columnsAggregateFunctions.add(Expression.Type.PATH);
            case DATA -> columnsAggregateFunctions.add(Expression.Type.DATA);
            case TEXT -> columnsAggregateFunctions.add(Expression.Type.TEXT);
            case NUMBER -> columnsAggregateFunctions.add(Expression.Type.NUMBER);
            default -> throw new ZQLException("Unhandled expression type; expression=" + expression);
        }
    }

    private void collectZnodes(ResultSet resultSet) throws ZQLException {
        if (statement.getZnodes() == null) {
            return;
        }

        try {
            collectZnodes(statement.getZnodes(), resultSet);
        } catch (Exception e) {
            throw new ZQLException("ZNodes collecting error; query=" + query, e);
        }
    }

    private void collectZnodes(Znode znode, ResultSet resultSet) throws Exception {
        switch (znode.getType()) {
            case COMMA -> {
                final ZnodeCollection znodeComma = (ZnodeCollection) znode;
                for (var item : znodeComma.getCollection()) {
                    collectZnodes(item, resultSet);
                }
            }
            case LIST -> {
                final ZnodeWrapper znodeList = (ZnodeWrapper) znode;
                final ZnodePath znodeWrapped = (ZnodePath) znodeList.getWrappedZnode();
                for (var childPath : curator.getChildren().forPath(znodeWrapped.getPath())) {
                    final String path = znodeWrapped.getPath() + "/" + childPath;
                    final byte[] bytes = curator.getData().forPath(path);
                    resultSet.znodes.put(path, bytes == null ? "" : new String(bytes, StandardCharsets.UTF_8));
                }
            }
            case PATH -> {
                final ZnodePath znodePath = (ZnodePath) znode;
                final String path = znodePath.getPath();
                final byte[] bytes = curator.getData().forPath(path);
                resultSet.znodes.put(path, bytes == null ? "" : new String(bytes, StandardCharsets.UTF_8));
            }
            default -> throw new ZQLException("Unhandled znode type; znode=" + znode);
        }
    }

    private void filterZnodes(ResultSet resultSet) throws ZQLException {
        if (statement.getConditions() == null) {
            return;
        }

        try {
            resultSet.znodes.entrySet().removeIf(entry -> !filterZnodes(statement.getConditions(), entry.getKey(), entry.getValue()));
        } catch (Exception e) {
            throw new ZQLException("ZNodes collecting error; query=" + query, e);
        }
    }

    private boolean filterZnodes(Condition condition, String path, String data) {
        switch (condition.getType()) {
            case AND -> {
                final ConditionCompositeOfCondition conditionsAnd = (ConditionCompositeOfCondition) condition;
                return filterZnodes(conditionsAnd.getLeft(), path, data) && filterZnodes(conditionsAnd.getRight(), path, data);
            }
            case OR -> {
                final ConditionCompositeOfCondition conditionsOr = (ConditionCompositeOfCondition) condition;
                return filterZnodes(conditionsOr.getLeft(), path, data) || filterZnodes(conditionsOr.getRight(), path, data);
            }
            case IN_BRACKETS -> {
                final ConditionWrapper conditionInBrackets = (ConditionWrapper) condition;
                return filterZnodes(conditionInBrackets.getWrappedCondition(), path, data);
            }
            case EQUALS -> {
                final ConditionCompositeOfExpression conditionEquals = (ConditionCompositeOfExpression) condition;
                final ExpressionString left = (ExpressionString) conditionEquals.getLeft();
                final ExpressionString right = (ExpressionString) conditionEquals.getRight();
                switch (left.getType()) {
                    case JSON -> {
                        return false;
                    }
                    case PATH -> {
                        return path.equals(right.getText());
                    }
                    case DATA -> {
                        return data.equals(right.getText());
                    }
                    // todo json and number
                    default -> throw new ZQLException("Can't handle equals condition; condition=" + condition + ", left=" + left + ", right=" + right);
                }
            }
            case NOT_EQUALS -> {
                final ConditionCompositeOfExpression conditionNotEquals = (ConditionCompositeOfExpression) condition;
                final ExpressionString left = (ExpressionString) conditionNotEquals.getLeft();
                final ExpressionString right = (ExpressionString) conditionNotEquals.getRight();
                switch (left.getType()) {
                    case JSON -> {
                        return false;
                    }
                    case PATH -> {
                        return !path.equals(right.getText());
                    }
                    case DATA -> {
                        return !data.equals(right.getText());
                    }
                    default -> throw new ZQLException("Can't handle not equals condition; condition=" + condition + ", left=" + left + ", right=" + right);
                }
            }
            case LIKE -> {
                final ConditionCompositeOfExpression conditionLike = (ConditionCompositeOfExpression) condition;
                final ExpressionString left = (ExpressionString) conditionLike.getLeft();
                final ExpressionString right = (ExpressionString) conditionLike.getRight();
                switch (left.getType()) {
                    case JSON -> {
                        return false;
                    }
                    case PATH -> {
                        return path.matches(right.getText());
                    }
                    case DATA -> {
                        return data.matches(right.getText());
                    }
                    default -> throw new ZQLException("Can't handle like condition; condition=" + condition + ", left=" + left + ", right=" + right);
                }
            }
            case NOT_LIKE -> {
                final ConditionCompositeOfExpression conditionNotLike = (ConditionCompositeOfExpression) condition;
                final ExpressionString left = (ExpressionString) conditionNotLike.getLeft();
                final ExpressionString right = (ExpressionString) conditionNotLike.getRight();
                switch (left.getType()) {
                    case JSON -> {
                        return false;
                    }
                    case PATH -> {
                        return !path.matches(right.getText());
                    }
                    case DATA -> {
                        return !data.matches(right.getText());
                    }
                    default -> throw new ZQLException("Can't handle not like condition; condition=" + condition + ", left=" + left + ", right=" + right);
                }
            }
            default -> throw new ZQLException("Unhandled condition type; condition=" + condition);
        }
    }

    private void collectRows(ResultSet resultSet) {
        if (statement.getExpressions() == null) {
            return;
        }

        try {
            resultSet.znodes.forEach((key, value) -> {
                final List<String> row = new ArrayList<>();
                collectRows(statement.getExpressions(), row, key, value);
                resultSet.rows.add(row);
            });
        } catch (Exception e) {
            throw new ZQLException("Rows collecting error; query=" + query, e);
        }
    }

    private void collectRows(Expression expression, List<String> row, String path, String data) {
        switch (expression.getType()) {
            case COMMA -> {
                final ExpressionCollection expressionComma = (ExpressionCollection) expression;
                expressionComma.getCollection().forEach(e -> collectRows(e, row, path, data));
            }
            case ALIAS -> {
                final ExpressionAlias selectExpressionAlias = (ExpressionAlias) expression;
                collectRows(expressionAlias.getWrappedExpression(), row, path, data);
            }
            case COUNT, SUM, AVG, MIN, MAX -> {
                final ExpressionWrapper expressionWrapper = (ExpressionWrapper) expression;
                collectRows(expressionWrapper.getWrappedExpression(), row, path, data);
            }
//            case JSON -> {
//                final Expression expressionJson = (Expression) expression;
//                row.add(expressionJson.getText());
//            }
            case PATH -> row.add(path);
            case DATA -> row.add(data);
            case TEXT, NUMBER -> {
                final ExpressionString expressionString = (ExpressionString) expression;
                row.add(expressionString.getText());
            }
            default -> throw new ZQLException("Unhandled expression type; expression=" + expression);
        }
    }

    private void groupRows(ResultSet resultSet) {
        if (statement.getGroups() == null && !isAllColumnsAggregateFunctions) {
            return;
        }

        try {
            final Map<String, List<String>> uniqueGroups = new HashMap<>();
            final int[] counter = {0};
            resultSet.rows.removeIf(row -> {
                final StringBuilder stringBuilder = new StringBuilder();

                for (int i = 0; i < row.size(); i++) {
                    final Expression.Type type = columnsAggregateFunctions.get(i);
                    switch (type) {
                        case COUNT, SUM, AVG, MIN, MAX -> {
                            // ...
                        }
                        case JSON, PATH, DATA, TEXT, NUMBER -> {
                            stringBuilder.append(row.get(i));
                        }
                        default -> throw new ZQLException("Unhandled expression type; type=" + type);
                    }
                }

                final String group = stringBuilder.toString();
                final List<String> groupRow = uniqueGroups.get(group);

                boolean result = false;

                if (groupRow == null) {
                    uniqueGroups.put(group, row);
                } else {
                    for (int i = 0; i < groupRow.size(); i++) {
                        final Expression.Type type = columnsAggregateFunctions.get(i);
                        switch (type) {
                            case COUNT -> {
                                try {
                                    final int buff = Integer.parseInt(groupRow.get(i)) + 1;
                                    groupRow.set(i, String.valueOf(buff));
                                } catch (NumberFormatException e) {
                                    // ...
                                } finally {
                                    result = true;
                                }
                            }
                            case SUM -> {
                                try {
                                    final float buff = Float.parseFloat(groupRow.get(i)) + Float.parseFloat(row.get(i));
                                    groupRow.set(i, String.valueOf(buff));
                                } catch (NumberFormatException e) {
                                    // ...
                                } finally {
                                    result = true;
                                }
                            }
                            case AVG -> {
                                try {
                                    final float groupRowValue = Float.parseFloat(groupRow.get(i));
                                    final float value = Float.parseFloat(row.get(i));
                                    groupRow.set(i, String.valueOf( groupRowValue + (value - groupRowValue) / (counter[0] + 1)) );
                                } catch (NumberFormatException e) {
                                    // ...
                                } finally {
                                    result = true;
                                }
                            }
                            case MIN -> {
                                try {
                                    final float buff = Math.min(Float.parseFloat(groupRow.get(i)), Float.parseFloat(row.get(i)));
                                    groupRow.set(i, String.valueOf(buff));
                                } catch (NumberFormatException e) {
                                    // ...
                                } finally {
                                    result = true;
                                }
                            }
                            case MAX -> {
                                try {
                                    final float buff = Math.max(Float.parseFloat(groupRow.get(i)), Float.parseFloat(row.get(i)));
                                    groupRow.set(i, String.valueOf(buff));
                                } catch (NumberFormatException e) {
                                    // ...
                                } finally {
                                    result = true;
                                }
                            }
                            case JSON, PATH, DATA, TEXT, NUMBER -> {
                                // ...
                            }
                            default -> throw new ZQLException("Unhandled expression type; type=" + type);
                        }
                    }
                }

                counter[0]++;
                return result;
            });
        } catch (Exception e) {
            throw new ZQLException("Rows grouping error; query=" + query, e);
        }
    }
}
