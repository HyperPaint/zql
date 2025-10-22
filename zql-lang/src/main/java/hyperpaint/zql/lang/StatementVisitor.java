package hyperpaint.zql.lang;

import hyperpaint.zql.lang.antlr4.ZQLBaseVisitor;
import hyperpaint.zql.lang.antlr4.ZQLParser;
import hyperpaint.zql.lang.condition.ConditionCompositeOfCondition;
import hyperpaint.zql.lang.expression.ExpressionCollection;
import hyperpaint.zql.lang.statement.Select;
import hyperpaint.zql.lang.statement.Statement;
import hyperpaint.zql.lang.znode.ZnodeCollection;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class StatementVisitor extends ZQLBaseVisitor<Statement> {
    static final StatementVisitor INSTANCE = new StatementVisitor();

    @Override
    public Statement visitSelect(ZQLParser.SelectContext ctx) {
        final ExpressionCollection selectExpressions = (ExpressionCollection) ExpressionVisitor.INSTANCE.visit(ctx.expressions(0));
        final ZnodeCollection fromZnodes = (ZnodeCollection) ZnodeVisitor.INSTANCE.visit(ctx.znodes());
        final ConditionCompositeOfCondition whereConditions = (ConditionCompositeOfCondition) ConditionVisitor.INSTANCE.visit(ctx.conditions(0));
        final ExpressionCollection groupByExpressions = (ExpressionCollection) ExpressionVisitor.INSTANCE.visit(ctx.expressions(1));
        final ConditionCompositeOfCondition havingConditions = (ConditionCompositeOfCondition) ConditionVisitor.INSTANCE.visit(ctx.conditions(1));
        final ExpressionCollection orderByExpressions = (ExpressionCollection) ExpressionVisitor.INSTANCE.visit(ctx.expressions(2));

        return new Select(Statement.Type.SELECT, selectExpressions, fromZnodes, whereConditions, groupByExpressions, havingConditions, orderByExpressions);
    }

//    @Override
//    public Statement visitSelectFrom(ZQLParser.SelectFromContext ctx) {
//        final Expression expression = ExpressionVisitor.INSTANCE.visit(ctx.expressions());
//        final Znode znodes = ZnodeVisitor.INSTANCE.visit(ctx.znodes());
//        return validate(new Statement(expression, znodes, null, null));
//    }
//
//    @Override
//    public Statement visitSelectFromWhere(ZQLParser.SelectFromWhereContext ctx) {
//        final Expression expression = ExpressionVisitor.INSTANCE.visit(ctx.expressions());
//        final Znode znodes = ZnodeVisitor.INSTANCE.visit(ctx.znodes());
//        final Condition conditions = ConditionVisitor.INSTANCE.visit(ctx.conditions());
//        return validate(new Statement(expression, znodes, conditions, null));
//    }
//
//    @Override
//    public Statement visitSelectFromWhereGroupBy(ZQLParser.SelectFromWhereGroupByContext ctx) {
//        final Expression expression = ExpressionVisitor.INSTANCE.visit(ctx.expressions());
//        final Znode znodes = ZnodeVisitor.INSTANCE.visit(ctx.znodes());
//        final Condition conditions = ConditionVisitor.INSTANCE.visit(ctx.conditions());
//        final AbstractGroup groups = GroupsVisitor.INSTANCE.visit(ctx.groups());
//
//        return (new Statement(expression, znodes, conditions, groups));
//    }
//
//    @Override
//    public Statement visitSelectFromGroupBy(ZQLParser.SelectFromGroupByContext ctx) {
//        final Expression expression = ExpressionVisitor.INSTANCE.visit(ctx.expressions());
//        final Znode znodes = ZnodeVisitor.INSTANCE.visit(ctx.znodes());
//        final AbstractGroup groups = GroupsVisitor.INSTANCE.visit(ctx.groups());
//        return validate(new Statement(expression, znodes, null, groups));
//    }
//
//    private Statement validate(Statement statement) {
//        final List<String> usedInGrouping = new ArrayList<>();
//        if (statement.getExpressions() != null) {
//            expressionsUsedInGrouping(statement.getExpressions(), usedInGrouping);
//        }
//
//        final List<String> notUsedInGrouping = new ArrayList<>();
//        if (statement.getExpressions() != null) {
//            expressionsNotUsedInGrouping(statement.getExpressions(), notUsedInGrouping);
//        }
//
//        final List<String> grouping = new ArrayList<>();
//        if (statement.getGroups() != null) {
//            groupings(statement.getGroups(), grouping);
//        }
//
//        // Если ни одно поле не используется в группировке и группировки нет, нечего проверять
//        if (usedInGrouping.isEmpty() && grouping.isEmpty()) {
//            return statement;
//        }
//
//        // Если все поля используются в группировке и группировки нет, нечего проверять
//        if (notUsedInGrouping.isEmpty() && grouping.isEmpty()) {
//            return statement;
//        }
//
//        //noinspection SlowListContainsAll
//        if (!notUsedInGrouping.containsAll(grouping)) {
//            throw new ZqlException("Can't validate statement, fields that are not used in the grouping are missing in group by; usedInGrouping=" + usedInGrouping + ", notUsedInGrouping=" + notUsedInGrouping + ", grouping=" + grouping);
//        }
//
//        //noinspection SlowListContainsAll
//        if (!grouping.containsAll(notUsedInGrouping)) {
//            throw new ZqlException("Can't validate statement, group by fields are missing in fields that are not used in grouping; usedInGrouping=" + usedInGrouping + ", notUsedInGrouping=" + notUsedInGrouping + ", grouping=" + grouping);
//        }
//
//        return statement;
//    }
//
//    private void groupings(AbstractGroup groups, List<String> resultList) {
//        switch (groups.getType()) {
//            case COMMA -> {
//                final GroupList groupComma = (GroupList) groups;
//                for (var item : groupComma.getList()) {
//                    groupings(item, resultList);
//                }
//            }
//            case GROUP -> {
//                final Group group = (Group) groups;
//                final ExpressionString expression = (ExpressionString) group.getExpression();
//                resultList.add(expression.getText());
//            }
//            default -> throw new ZqlException("Can't validate statement, unhandled group type");
//        }
//    }
//
//    private void expressionsUsedInGrouping(Expression expression, List<String> resultList) {
//        switch (expression.getType()) {
//            case COMMA -> {
//                final ExpressionCollection expressionComma = (ExpressionCollection) expression;
//                for (var item : expressionComma.getCollection()) {
//                    expressionsUsedInGrouping(item, resultList);
//                }
//            }
//            case ALIAS -> {
//                final ExpressionAlias expressionAlias = (ExpressionAlias) expression;
//                expressionsUsedInGrouping(expressionAlias.getWrappedExpression(), resultList);
//            }
//            case COUNT, SUM, AVG, MIN, MAX -> {
//                final ExpressionWrapper expressionWrapper = (ExpressionWrapper) expression;
//                final ExpressionString wrappedExpression = (ExpressionString) expressionWrapper.getWrappedExpression();
//                resultList.add(wrappedExpression.getText());
//            }
//            case JSON, PATH, DATA, TEXT, NUMBER -> {
//                // ...
//            }
//            default -> throw new ZqlException("Can't validate statement, unhandled expression type");
//        }
//    }
//
//    private void expressionsNotUsedInGrouping(Expression expression, List<String> resultList) {
//        switch (expression.getType()) {
//            case COMMA -> {
//                final ExpressionCollection expressionComma = (ExpressionCollection) expression;
//                for (var item : expressionComma.getCollection()) {
//                    expressionsNotUsedInGrouping(item, resultList);
//                }
//            }
//            case ALIAS -> {
//                final ExpressionAlias expressionAlias = (ExpressionAlias) expression;
//                expressionsNotUsedInGrouping(expressionAlias.getWrappedExpression(), resultList);
//            }
//            case COUNT, SUM, AVG, MIN, MAX -> {
//                // ...
//            }
//            case JSON, PATH, DATA, TEXT, NUMBER -> {
//                final ExpressionString expression1 = (ExpressionString) expression;
//                resultList.add(expression1.getText());
//            }
//            default -> throw new ZqlException("Can't validate statement, unhandled expression type");
//        }
//    }
}
