package hyperpaint.zql.lang;

import hyperpaint.zql.lang.antlr4.ZQLLexer;
import hyperpaint.zql.lang.antlr4.ZQLParser;
import hyperpaint.zql.lang.condition.Condition;
import hyperpaint.zql.lang.condition.ConditionCompositeOfCondition;
import hyperpaint.zql.lang.condition.ConditionCompositeOfExpression;
import hyperpaint.zql.lang.condition.ConditionWrapper;
import hyperpaint.zql.lang.expression.*;
import hyperpaint.zql.lang.expression.order_by.OrderByExpressionWrapper;
import hyperpaint.zql.lang.expression.select.SelectExpressionWrapper;
import hyperpaint.zql.lang.statement.Select;
import hyperpaint.zql.lang.statement.Statement;
import hyperpaint.zql.lang.znode.Znode;
import hyperpaint.zql.lang.znode.ZnodeCollection;
import hyperpaint.zql.lang.znode.ZnodePath;
import hyperpaint.zql.lang.znode.ZnodeWrapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ZQL {
    public static Statement parse(String query) throws ZQLException {
        try {
            final CharStream charStream = CharStreams.fromString(query);
            final ZQLLexer lexer = new ZQLLexer(charStream);
            final CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            final ZQLParser parser = new ZQLParser(tokenStream);
            final ParseTree parseTree = parser.zql();
            return ZQLVisitor.INSTANCE.visit(parseTree);
        } catch (Exception e) {
            throw new ZQLException(e);
        }
    }

    public static String combine(Statement statement) {
        return switch (statement.getClass().getName()) {
            case "hyperpaint.zql.lang.statement.Select" -> combine((Select) statement);
            default -> throw new IllegalArgumentException("Unexpected value: " + statement.getClass().getName());
        };
    }

    private static String combine(Select select) {
        final StringBuilder result = new StringBuilder();

        if (select.hasSelectExpression()) {
            result.append("select ");
            result.append(combine(select.getSelectExpression()));
            result.append(" ");
        }

        if (select.hasFromZnode()) {
            result.append("from ");
            result.append(combine(select.getFromZnode()));
            result.append(" ");
        }

        if (select.hasWhereCondition()) {
            result.append("where ");
            result.append(combine(select.getWhereCondition()));
            result.append(" ");
        }

        if (select.hasGroupByExpression()) {
            result.append("group by ");
            result.append(combine(select.getGroupByExpression()));
            result.append(" ");
        }

        if (select.hasHavingCondition()) {
            result.append("having ");
            result.append(combine(select.getHavingCondition()));
            result.append(" ");
        }

        if (select.hasOrderByExpression()) {
            result.append("order by ");
            result.append(combine(select.getOrderByExpression()));
            result.append(" ");
        }

        return result.toString().trim() + ";";
    }

    private static String combine(Expression expression) {
        switch (expression.getType()) {
            case COMMA -> {
                final ExpressionCollection expressionCollection = (ExpressionCollection) expression;
                final StringBuilder result = new StringBuilder();

                for (var iterator = expressionCollection.getList().iterator(); iterator.hasNext(); ) {
                    var item = iterator.next();

                    result.append(combine(item));

                    if (iterator.hasNext()) {
                        result.append(", ");
                    }
                 }

                return result.toString();
            }
            case ALIAS -> {
                final SelectExpressionWrapper selectExpressionWrapper = (SelectExpressionWrapper) expression;
                if (selectExpressionWrapper.getAlias().contains("'")) {
                    return combine(selectExpressionWrapper.getWrappedExpression()) + " as " + "\"" + selectExpressionWrapper.getAlias() + "\"";
                } else if (selectExpressionWrapper.getAlias().contains("\"")) {
                    return combine(selectExpressionWrapper.getWrappedExpression()) + " as " + "'" + selectExpressionWrapper.getAlias() + "'";
                } else {
                    return combine(selectExpressionWrapper.getWrappedExpression()) + " as " + selectExpressionWrapper.getAlias();
                }
            }
            case ORDER -> {
                final OrderByExpressionWrapper orderByExpressionWrapper = (OrderByExpressionWrapper) expression;
                if (orderByExpressionWrapper.isAscending()) {
                    return combine(orderByExpressionWrapper.getWrappedExpression()) + " asc";
                } else {
                    return combine(orderByExpressionWrapper.getWrappedExpression()) + " desc";
                }
            }
            case COUNT -> {
                final ExpressionWrapper expressionWrapper = (ExpressionWrapper) expression;
                return "count(" + combine(expressionWrapper.getWrappedExpression()) + ")";
            }
            case SUM -> {
                final ExpressionWrapper expressionWrapper = (ExpressionWrapper) expression;
                return "sum(" + combine(expressionWrapper.getWrappedExpression()) + ")";
            }
            case AVG -> {
                final ExpressionWrapper expressionWrapper = (ExpressionWrapper) expression;
                return "avg(" + combine(expressionWrapper.getWrappedExpression()) + ")";
            }
            case MIN -> {
                final ExpressionWrapper expressionWrapper = (ExpressionWrapper) expression;
                return "min(" + combine(expressionWrapper.getWrappedExpression()) + ")";
            }
            case MAX -> {
                final ExpressionWrapper expressionWrapper = (ExpressionWrapper) expression;
                return "max(" + combine(expressionWrapper.getWrappedExpression()) + ")";
            }
            case JSON_PATH -> {
                final ExpressionWrapper2 expressionWrapper = (ExpressionWrapper2) expression;
                return "json(" + combine(expressionWrapper.getWrappedExpression1()) + ", " + combine(expressionWrapper.getWrappedExpression2()) + ")";
            }
            case TEXT -> {
                final ExpressionString expressionString = (ExpressionString) expression;
                return "\"" + expressionString.getText() + "\"";
            }
            case NUMBER -> {
                final ExpressionNumber expressionNumber = (ExpressionNumber) expression;
                return String.valueOf(expressionNumber.getNumber());
            }
            case IDENTIFIER -> {
                final ExpressionString expressionString = (ExpressionString) expression;
                return expressionString.getText();
            }
            default -> throw new IllegalArgumentException("Unexpected value: " + expression.getType());
        }
    }

    private static String combine(Znode znode) {
        switch (znode.getType()) {
            case COMMA -> {
                final ZnodeCollection znodeCollection = (ZnodeCollection) znode;
                final StringBuilder result = new StringBuilder();

                for (var iterator = znodeCollection.getList().iterator(); iterator.hasNext(); ) {
                    var item = iterator.next();

                    result.append(combine(item));

                    if (iterator.hasNext()) {
                        result.append(", ");
                    }
                }

                return result.toString();
            }
            case LIST -> {
                final ZnodeWrapper znodeWrapper = (ZnodeWrapper) znode;
                return "ls/" + combine(znodeWrapper.getWrappedZnode());
            }
            case PATH -> {
                final ZnodePath znodePath = (ZnodePath) znode;
                return znodePath.getPath();
            }
            default -> throw new IllegalArgumentException("Unexpected value: " + znode.getType());
        }
    }

    private static String combine(Condition condition) {
        switch (condition.getType()) {
            case AND -> {
                final ConditionCompositeOfCondition conditionCompositeOfCondition = (ConditionCompositeOfCondition) condition;
                return combine(conditionCompositeOfCondition.getLeft()) + " and " + combine(conditionCompositeOfCondition.getRight());
            }
            case OR -> {
                final ConditionCompositeOfCondition conditionCompositeOfCondition = (ConditionCompositeOfCondition) condition;
                return combine(conditionCompositeOfCondition.getLeft()) + " or " + combine(conditionCompositeOfCondition.getRight());
            }
            case IN_BRACKETS -> {
                final ConditionWrapper conditionWrapper = (ConditionWrapper) condition;
                return "(" + combine(conditionWrapper.getWrappedCondition()) + ")";
            }
            case EQUALS -> {
                final ConditionCompositeOfExpression conditionCompositeOfExpression = (ConditionCompositeOfExpression) condition;
                return combine(conditionCompositeOfExpression.getLeft()) + " == " + combine(conditionCompositeOfExpression.getRight());
            }
            case NOT_EQUALS -> {
                final ConditionCompositeOfExpression conditionCompositeOfExpression = (ConditionCompositeOfExpression) condition;
                return combine(conditionCompositeOfExpression.getLeft()) + " != " + combine(conditionCompositeOfExpression.getRight());
            }
            case LIKE -> {
                final ConditionCompositeOfExpression conditionCompositeOfExpression = (ConditionCompositeOfExpression) condition;
                return combine(conditionCompositeOfExpression.getLeft()) + " =~ " + combine(conditionCompositeOfExpression.getRight());
            }
            case NOT_LIKE -> {
                final ConditionCompositeOfExpression conditionCompositeOfExpression = (ConditionCompositeOfExpression) condition;
                return combine(conditionCompositeOfExpression.getLeft()) + " !~ " + combine(conditionCompositeOfExpression.getRight());
            }
            default -> throw new IllegalArgumentException("Unexpected value: " + condition.getType());
        }
    }
}
