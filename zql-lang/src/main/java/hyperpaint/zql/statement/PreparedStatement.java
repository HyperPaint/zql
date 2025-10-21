package hyperpaint.zql.statement;

import hyperpaint.zql.ZqlException;
import hyperpaint.zql.ZqlVisitor;
import hyperpaint.zql.antlr4.ZQLLexer;
import hyperpaint.zql.antlr4.ZQLParser;
import hyperpaint.zql.condition.AbstractCondition;
import hyperpaint.zql.condition.Condition;
import hyperpaint.zql.condition.ConditionsLogic;
import hyperpaint.zql.condition.ConditionsWrapper;
import hyperpaint.zql.expression.*;
import hyperpaint.zql.znode.AbstractZnode;
import hyperpaint.zql.znode.Znode;
import hyperpaint.zql.znode.ZnodeList;
import hyperpaint.zql.znode.ZnodeWrapper;
import lombok.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.curator.framework.CuratorFramework;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class PreparedStatement {
    private @Setter CuratorFramework curator;

    private final String query;
    private Statement statement;

    private final List<String> columns = new ArrayList<>();
    private final List<ExpressionType> columnsAggregateFunctions = new ArrayList<>();
    private boolean isAllColumnsAggregateFunctions = false;

    public PreparedStatement(@NonNull CuratorFramework curator, @NonNull String query) {
        this.curator = curator;
        this.query = query;
    }

    public static class ResultSet {
        private final @Getter List<String> columns;
        private final @Getter List<List<String>> rows;
        private final @Getter Map<String, String> znodes;

        private ResultSet(PreparedStatement preparedStatement) {
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

    private void parse() throws ZqlException {
        try {
            final CharStream charStream = CharStreams.fromString(query);
            final ZQLLexer lexer = new ZQLLexer(charStream);
            final CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            final ZQLParser parser = new ZQLParser(tokenStream);
            final ParseTree parseTree = parser.zql();
            statement = ZqlVisitor.INSTANCE.visit(parseTree);
        } catch (Exception e) {
            throw new ZqlException("Query parsing error; query=" + query, e);
        }
    }

    private void analyze() throws ZqlException {
        if (statement.getExpressions() == null) {
            return;
        }

        try {
            analyzeColumns(statement.getExpressions());
        } catch (Exception e) {
            throw new ZqlException("Analyze columns error; query=" + query, e);
        }

        try {
            analyzeAggregation(statement.getExpressions());
            isAllColumnsAggregateFunctions = columnsAggregateFunctions.stream().allMatch(type -> type == ExpressionType.COUNT || type == ExpressionType.SUM || type == ExpressionType.AVG || type == ExpressionType.MIN || type == ExpressionType.MAX);
        } catch (Exception e) {
            throw new ZqlException("Analyze aggregation error; query=" + query, e);
        }
    }

    private void analyzeColumns(AbstractExpression expression) {
        switch (expression.getType()) {
            case COMMA -> {
                final ExpressionList expressionComma = (ExpressionList) expression;
                expressionComma.getList().forEach(this::analyzeColumns);
            }
            case ALIAS -> {
                final ExpressionAlias expressionAlias = (ExpressionAlias) expression;
                columns.add(expressionAlias.getAlias());
            }
            case COUNT -> {
                final ExpressionWrapper expressionWrapper = (ExpressionWrapper) expression;
                final Expression wrappedExpression = (Expression) expressionWrapper.getWrappedExpression();
                columns.add("count(" + wrappedExpression.getText() + ")");
            }
            case SUM -> {
                final ExpressionWrapper expressionWrapper = (ExpressionWrapper) expression;
                final Expression wrappedExpression = (Expression) expressionWrapper.getWrappedExpression();
                columns.add("sum(" + wrappedExpression.getText() + ")");
            }
            case AVG -> {
                final ExpressionWrapper expressionWrapper = (ExpressionWrapper) expression;
                final Expression wrappedExpression = (Expression) expressionWrapper.getWrappedExpression();
                columns.add("avg(" + wrappedExpression.getText() + ")");
            }
            case MIN -> {
                final ExpressionWrapper expressionWrapper = (ExpressionWrapper) expression;
                final Expression wrappedExpression = (Expression) expressionWrapper.getWrappedExpression();
                columns.add("min(" + wrappedExpression.getText() + ")");
            }
            case MAX -> {
                final ExpressionWrapper expressionWrapper = (ExpressionWrapper) expression;
                final Expression wrappedExpression = (Expression) expressionWrapper.getWrappedExpression();
                columns.add("max(" + wrappedExpression.getText() + ")");
            }
            case JSON -> columns.add("json");
            case PATH -> columns.add("path");
            case DATA -> columns.add("data");
            case TEXT -> columns.add("text");
            case NUMBER -> columns.add("number");
            default -> throw new ZqlException("Unhandled expression type; expression=" + expression);
        }
    }

    private void analyzeAggregation(AbstractExpression expression) {
        switch (expression.getType()) {
            case COMMA -> {
                final ExpressionList expressionComma = (ExpressionList) expression;
                expressionComma.getList().forEach(this::analyzeAggregation);
            }
            case ALIAS -> {
                final ExpressionAlias expressionAlias = (ExpressionAlias) expression;
                analyzeAggregation(expressionAlias.getWrappedExpression());
            }
            case COUNT -> columnsAggregateFunctions.add(ExpressionType.COUNT);
            case SUM -> columnsAggregateFunctions.add(ExpressionType.SUM);
            case AVG -> columnsAggregateFunctions.add(ExpressionType.AVG);
            case MIN -> columnsAggregateFunctions.add(ExpressionType.MIN);
            case MAX -> columnsAggregateFunctions.add(ExpressionType.MAX);
            case JSON -> columnsAggregateFunctions.add(ExpressionType.JSON);
            case PATH -> columnsAggregateFunctions.add(ExpressionType.PATH);
            case DATA -> columnsAggregateFunctions.add(ExpressionType.DATA);
            case TEXT -> columnsAggregateFunctions.add(ExpressionType.TEXT);
            case NUMBER -> columnsAggregateFunctions.add(ExpressionType.NUMBER);
            default -> throw new ZqlException("Unhandled expression type; expression=" + expression);
        }
    }

    private void collectZnodes(ResultSet resultSet) throws ZqlException {
        if (statement.getZnodes() == null) {
            return;
        }

        try {
            collectZnodes(statement.getZnodes(), resultSet);
        } catch (Exception e) {
            throw new ZqlException("ZNodes collecting error; query=" + query, e);
        }
    }

    private void collectZnodes(AbstractZnode znode, ResultSet resultSet) throws Exception {
        switch (znode.getType()) {
            case COMMA -> {
                final ZnodeList znodeComma = (ZnodeList) znode;
                for (var item : znodeComma.getList()) {
                    collectZnodes(item, resultSet);
                }
            }
            case LIST -> {
                final ZnodeWrapper znodeList = (ZnodeWrapper) znode;
                final Znode znodeWrapped = (Znode) znodeList.getWrappedZnode();
                for (var childPath : curator.getChildren().forPath(znodeWrapped.getPath())) {
                    final String path = znodeWrapped.getPath() + "/" + childPath;
                    final byte[] bytes = curator.getData().forPath(path);
                    resultSet.znodes.put(path, bytes == null ? "" : new String(bytes, StandardCharsets.UTF_8));
                }
            }
            case PATH -> {
                final Znode znodePath = (Znode) znode;
                final String path = znodePath.getPath();
                final byte[] bytes = curator.getData().forPath(path);
                resultSet.znodes.put(path, bytes == null ? "" : new String(bytes, StandardCharsets.UTF_8));
            }
            default -> throw new ZqlException("Unhandled znode type; znode=" + znode);
        }
    }

    private void filterZnodes(ResultSet resultSet) throws ZqlException {
        if (statement.getConditions() == null) {
            return;
        }

        try {
            resultSet.znodes.entrySet().removeIf(entry -> !filterZnodes(statement.getConditions(), entry.getKey(), entry.getValue()));
        } catch (Exception e) {
            throw new ZqlException("ZNodes collecting error; query=" + query, e);
        }
    }

    private boolean filterZnodes(AbstractCondition condition, String path, String data) {
        switch (condition.getType()) {
            case AND -> {
                final ConditionsLogic conditionsAnd = (ConditionsLogic) condition;
                return filterZnodes(conditionsAnd.getLeft(), path, data) && filterZnodes(conditionsAnd.getRight(), path, data);
            }
            case OR -> {
                final ConditionsLogic conditionsOr = (ConditionsLogic) condition;
                return filterZnodes(conditionsOr.getLeft(), path, data) || filterZnodes(conditionsOr.getRight(), path, data);
            }
            case IN_BRACKETS -> {
                final ConditionsWrapper conditionInBrackets = (ConditionsWrapper) condition;
                return filterZnodes(conditionInBrackets.getWrappedConditions(), path, data);
            }
            case EQUALS -> {
                final Condition conditionEquals = (Condition) condition;
                final Expression left = (Expression) conditionEquals.getLeft();
                final Expression right = (Expression) conditionEquals.getRight();
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
                    default -> throw new ZqlException("Can't handle equals condition; condition=" + condition + ", left=" + left + ", right=" + right);
                }
            }
            case NOT_EQUALS -> {
                final Condition conditionNotEquals = (Condition) condition;
                final Expression left = (Expression) conditionNotEquals.getLeft();
                final Expression right = (Expression) conditionNotEquals.getRight();
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
                    default -> throw new ZqlException("Can't handle not equals condition; condition=" + condition + ", left=" + left + ", right=" + right);
                }
            }
            case LIKE -> {
                final Condition conditionLike = (Condition) condition;
                final Expression left = (Expression) conditionLike.getLeft();
                final Expression right = (Expression) conditionLike.getRight();
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
                    default -> throw new ZqlException("Can't handle like condition; condition=" + condition + ", left=" + left + ", right=" + right);
                }
            }
            case NOT_LIKE -> {
                final Condition conditionNotLike = (Condition) condition;
                final Expression left = (Expression) conditionNotLike.getLeft();
                final Expression right = (Expression) conditionNotLike.getRight();
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
                    default -> throw new ZqlException("Can't handle not like condition; condition=" + condition + ", left=" + left + ", right=" + right);
                }
            }
            default -> throw new ZqlException("Unhandled condition type; condition=" + condition);
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
            throw new ZqlException("Rows collecting error; query=" + query, e);
        }
    }

    private void collectRows(AbstractExpression expression, List<String> row, String path, String data) {
        switch (expression.getType()) {
            case COMMA -> {
                final ExpressionList expressionComma = (ExpressionList) expression;
                expressionComma.getList().forEach(e -> collectRows(e, row, path, data));
            }
            case ALIAS -> {
                final ExpressionAlias expressionAlias = (ExpressionAlias) expression;
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
                final Expression expressionText = (Expression) expression;
                row.add(expressionText.getText());
            }
            default -> throw new ZqlException("Unhandled expression type; expression=" + expression);
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
                    final ExpressionType type = columnsAggregateFunctions.get(i);
                    switch (type) {
                        case COUNT, SUM, AVG, MIN, MAX -> {
                            // ...
                        }
                        case JSON, PATH, DATA, TEXT, NUMBER -> {
                            stringBuilder.append(row.get(i));
                        }
                        default -> throw new ZqlException("Unhandled expression type; type=" + type);
                    }
                }

                final String group = stringBuilder.toString();
                final List<String> groupRow = uniqueGroups.get(group);

                boolean result = false;

                if (groupRow == null) {
                    uniqueGroups.put(group, row);
                } else {
                    for (int i = 0; i < groupRow.size(); i++) {
                        final ExpressionType type = columnsAggregateFunctions.get(i);
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
                            default -> throw new ZqlException("Unhandled expression type; type=" + type);
                        }
                    }
                }

                counter[0]++;
                return result;
            });
        } catch (Exception e) {
            throw new ZqlException("Rows grouping error; query=" + query, e);
        }
    }
}
