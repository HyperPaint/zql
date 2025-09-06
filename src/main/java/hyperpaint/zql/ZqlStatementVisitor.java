package hyperpaint.zql;

import hyperpaint.zql.antlr4.ZQLBaseVisitor;
import hyperpaint.zql.antlr4.ZQLParser;
import hyperpaint.zql.condition.AbstractCondition;
import hyperpaint.zql.expression.*;
import hyperpaint.zql.groups.AbstractGroup;
import hyperpaint.zql.groups.Group;
import hyperpaint.zql.groups.GroupList;
import hyperpaint.zql.statement.Statement;
import hyperpaint.zql.znode.AbstractZnode;

import java.util.ArrayList;
import java.util.List;

class ZqlStatementVisitor extends ZQLBaseVisitor<Statement> {
    static final ZqlStatementVisitor INSTANCE = new ZqlStatementVisitor();

    @Override
    public Statement visitSelectFrom(ZQLParser.SelectFromContext ctx) {
        final AbstractExpression expression = ZqlExpressionsVisitor.INSTANCE.visit(ctx.expressions());
        final AbstractZnode znodes = ZqlZnodesVisitor.INSTANCE.visit(ctx.znodes());
        return validate(new Statement(expression, znodes, null, null));
    }

    @Override
    public Statement visitSelectFromWhere(ZQLParser.SelectFromWhereContext ctx) {
        final AbstractExpression expression = ZqlExpressionsVisitor.INSTANCE.visit(ctx.expressions());
        final AbstractZnode znodes = ZqlZnodesVisitor.INSTANCE.visit(ctx.znodes());
        final AbstractCondition conditions = ZqlConditionsVisitor.INSTANCE.visit(ctx.conditions());
        return validate(new Statement(expression, znodes, conditions, null));
    }

    @Override
    public Statement visitSelectFromWhereGroupBy(ZQLParser.SelectFromWhereGroupByContext ctx) {
        final AbstractExpression expression = ZqlExpressionsVisitor.INSTANCE.visit(ctx.expressions());
        final AbstractZnode znodes = ZqlZnodesVisitor.INSTANCE.visit(ctx.znodes());
        final AbstractCondition conditions = ZqlConditionsVisitor.INSTANCE.visit(ctx.conditions());
        final AbstractGroup groups = ZqlGroupsVisitor.INSTANCE.visit(ctx.groups());

        return (new Statement(expression, znodes, conditions, groups));
    }

    @Override
    public Statement visitSelectFromGroupBy(ZQLParser.SelectFromGroupByContext ctx) {
        final AbstractExpression expression = ZqlExpressionsVisitor.INSTANCE.visit(ctx.expressions());
        final AbstractZnode znodes = ZqlZnodesVisitor.INSTANCE.visit(ctx.znodes());
        final AbstractGroup groups = ZqlGroupsVisitor.INSTANCE.visit(ctx.groups());
        return validate(new Statement(expression, znodes, null, groups));
    }

    private Statement validate(Statement statement) {
        final List<String> usedInGrouping = new ArrayList<>();
        if (statement.getExpressions() != null) {
            expressionsUsedInGrouping(statement.getExpressions(), usedInGrouping);
        }

        final List<String> notUsedInGrouping = new ArrayList<>();
        if (statement.getExpressions() != null) {
            expressionsNotUsedInGrouping(statement.getExpressions(), notUsedInGrouping);
        }

        final List<String> grouping = new ArrayList<>();
        if (statement.getGroups() != null) {
            groupings(statement.getGroups(), grouping);
        }

        // Если ни одно поле не используется в группировке и группировки нет, нечего проверять
        if (usedInGrouping.isEmpty() && grouping.isEmpty()) {
            return statement;
        }

        // Если все поля используются в группировке и группировки нет, нечего проверять
        if (notUsedInGrouping.isEmpty() && grouping.isEmpty()) {
            return statement;
        }

        //noinspection SlowListContainsAll
        if (!notUsedInGrouping.containsAll(grouping)) {
            throw new ZqlException("Can't validate statement, fields that are not used in the grouping are missing in group by; usedInGrouping=" + usedInGrouping + ", notUsedInGrouping=" + notUsedInGrouping + ", grouping=" + grouping);
        }

        //noinspection SlowListContainsAll
        if (!grouping.containsAll(notUsedInGrouping)) {
            throw new ZqlException("Can't validate statement, group by fields are missing in fields that are not used in grouping; usedInGrouping=" + usedInGrouping + ", notUsedInGrouping=" + notUsedInGrouping + ", grouping=" + grouping);
        }

        return statement;
    }

    private void groupings(AbstractGroup groups, List<String> resultList) {
        switch (groups.getType()) {
            case COMMA -> {
                final GroupList groupComma = (GroupList) groups;
                for (var item : groupComma.getList()) {
                    groupings(item, resultList);
                }
            }
            case GROUP -> {
                final Group group = (Group) groups;
                final Expression expression = (Expression) group.getExpression();
                resultList.add(expression.getText());
            }
            default -> throw new ZqlException("Can't validate statement, unhandled group type");
        }
    }

    private void expressionsUsedInGrouping(AbstractExpression expression, List<String> resultList) {
        switch (expression.getType()) {
            case COMMA -> {
                final ExpressionList expressionComma = (ExpressionList) expression;
                for (var item : expressionComma.getList()) {
                    expressionsUsedInGrouping(item, resultList);
                }
            }
            case ALIAS -> {
                final ExpressionAlias expressionAlias = (ExpressionAlias) expression;
                expressionsUsedInGrouping(expressionAlias.getWrappedExpression(), resultList);
            }
            case COUNT, SUM, AVG, MIN, MAX -> {
                final ExpressionWrapper expressionWrapper = (ExpressionWrapper) expression;
                final Expression wrappedExpression = (Expression) expressionWrapper.getWrappedExpression();
                resultList.add(wrappedExpression.getText());
            }
            case JSON, PATH, DATA, TEXT, NUMBER -> {
                // ...
            }
            default -> throw new ZqlException("Can't validate statement, unhandled expression type");
        }
    }

    private void expressionsNotUsedInGrouping(AbstractExpression expression, List<String> resultList) {
        switch (expression.getType()) {
            case COMMA -> {
                final ExpressionList expressionComma = (ExpressionList) expression;
                for (var item : expressionComma.getList()) {
                    expressionsNotUsedInGrouping(item, resultList);
                }
            }
            case ALIAS -> {
                final ExpressionAlias expressionAlias = (ExpressionAlias) expression;
                expressionsNotUsedInGrouping(expressionAlias.getWrappedExpression(), resultList);
            }
            case COUNT, SUM, AVG, MIN, MAX -> {
                // ...
            }
            case JSON, PATH, DATA, TEXT, NUMBER -> {
                final Expression expression1 = (Expression) expression;
                resultList.add(expression1.getText());
            }
            default -> throw new ZqlException("Can't validate statement, unhandled expression type");
        }
    }
}
