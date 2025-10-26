package hyperpaint.zql.lang;

import hyperpaint.zql.lang.antlr4.ZQLBaseVisitor;
import hyperpaint.zql.lang.antlr4.ZQLParser;
import hyperpaint.zql.lang.condition.Condition;
import hyperpaint.zql.lang.condition.ConditionCompositeOfCondition;
import hyperpaint.zql.lang.condition.ConditionCompositeOfExpression;
import hyperpaint.zql.lang.condition.ConditionWrapper;
import hyperpaint.zql.lang.expression.Expression;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ConditionVisitor extends ZQLBaseVisitor<Condition> {
    static final ConditionVisitor INSTANCE = new ConditionVisitor();

    // region Where

    @Override
    public Condition visitWhereConditionsAndConditions(ZQLParser.WhereConditionsAndConditionsContext ctx) {
        final Condition left = visit(ctx.where_conditions(0));
        final Condition right = visit(ctx.where_conditions(1));

        return new ConditionCompositeOfCondition(Condition.Type.AND, left, right);
    }

    @Override
    public Condition visitWhereConditionsOrConditions(ZQLParser.WhereConditionsOrConditionsContext ctx) {
        final Condition left = visit(ctx.where_conditions(0));
        final Condition right = visit(ctx.where_conditions(1));

        return new ConditionCompositeOfCondition(Condition.Type.OR, left, right);
    }

    @Override
    public Condition visitWhereConditionsInBrackets(ZQLParser.WhereConditionsInBracketsContext ctx) {
        return new ConditionWrapper(Condition.Type.IN_BRACKETS, visit(ctx.where_conditions()));
    }

    @Override
    public Condition visitWhereConditionsBase(ZQLParser.WhereConditionsBaseContext ctx) {
        return visit(ctx.where_condition());
    }

    @Override
    public Condition visitWhereConditionEquals(ZQLParser.WhereConditionEqualsContext ctx) {
        final Expression left = ExpressionVisitor.INSTANCE.visit(ctx.where_condition_expression(0));
        final Expression right = ExpressionVisitor.INSTANCE.visit(ctx.where_condition_expression(1));

        return new ConditionCompositeOfExpression(Condition.Type.EQUALS, left, right);
    }

    @Override
    public Condition visitWhereConditionNotEquals(ZQLParser.WhereConditionNotEqualsContext ctx) {
        final Expression left = ExpressionVisitor.INSTANCE.visit(ctx.where_condition_expression(0));
        final Expression right = ExpressionVisitor.INSTANCE.visit(ctx.where_condition_expression(1));

        return new ConditionCompositeOfExpression(Condition.Type.NOT_EQUALS, left, right);
    }

    @Override
    public Condition visitWhereConditionLike(ZQLParser.WhereConditionLikeContext ctx) {
        final Expression left = ExpressionVisitor.INSTANCE.visit(ctx.where_condition_expression(0));
        final Expression right = ExpressionVisitor.INSTANCE.visit(ctx.where_condition_expression(1));

        return new ConditionCompositeOfExpression(Condition.Type.LIKE, left, right);
    }

    @Override
    public Condition visitWhereConditionNotLike(ZQLParser.WhereConditionNotLikeContext ctx) {
        final Expression left = ExpressionVisitor.INSTANCE.visit(ctx.where_condition_expression(0));
        final Expression right = ExpressionVisitor.INSTANCE.visit(ctx.where_condition_expression(1));

        return new ConditionCompositeOfExpression(Condition.Type.NOT_LIKE, left, right);
    }

    // endregion

    // region Having

    @Override
    public Condition visitHavingConditionsAndConditions(ZQLParser.HavingConditionsAndConditionsContext ctx) {
        final Condition left = visit(ctx.having_conditions(0));
        final Condition right = visit(ctx.having_conditions(1));

        return new ConditionCompositeOfCondition(Condition.Type.AND, left, right);
    }

    @Override
    public Condition visitHavingConditionsOrConditions(ZQLParser.HavingConditionsOrConditionsContext ctx) {
        final Condition left = visit(ctx.having_conditions(0));
        final Condition right = visit(ctx.having_conditions(1));

        return new ConditionCompositeOfCondition(Condition.Type.OR, left, right);
    }

    @Override
    public Condition visitHavingConditionsInBrackets(ZQLParser.HavingConditionsInBracketsContext ctx) {
        return new ConditionWrapper(Condition.Type.IN_BRACKETS, visit(ctx.having_conditions()));
    }

    @Override
    public Condition visitHavingConditionsBase(ZQLParser.HavingConditionsBaseContext ctx) {
        return visit(ctx.having_condition());
    }

    @Override
    public Condition visitHavingConditionEquals(ZQLParser.HavingConditionEqualsContext ctx) {
        final Expression left = ExpressionVisitor.INSTANCE.visit(ctx.having_condition_expression(0));
        final Expression right = ExpressionVisitor.INSTANCE.visit(ctx.having_condition_expression(1));

        return new ConditionCompositeOfExpression(Condition.Type.EQUALS, left, right);
    }

    @Override
    public Condition visitHavingConditionNotEquals(ZQLParser.HavingConditionNotEqualsContext ctx) {
        final Expression left = ExpressionVisitor.INSTANCE.visit(ctx.having_condition_expression(0));
        final Expression right = ExpressionVisitor.INSTANCE.visit(ctx.having_condition_expression(1));

        return new ConditionCompositeOfExpression(Condition.Type.NOT_EQUALS, left, right);
    }

    @Override
    public Condition visitHavingConditionLike(ZQLParser.HavingConditionLikeContext ctx) {
        final Expression left = ExpressionVisitor.INSTANCE.visit(ctx.having_condition_expression(0));
        final Expression right = ExpressionVisitor.INSTANCE.visit(ctx.having_condition_expression(1));

        return new ConditionCompositeOfExpression(Condition.Type.LIKE, left, right);
    }

    @Override
    public Condition visitHavingConditionNotLike(ZQLParser.HavingConditionNotLikeContext ctx) {
        final Expression left = ExpressionVisitor.INSTANCE.visit(ctx.having_condition_expression(0));
        final Expression right = ExpressionVisitor.INSTANCE.visit(ctx.having_condition_expression(1));

        return new ConditionCompositeOfExpression(Condition.Type.NOT_LIKE, left, right);
    }

    // endregion
}
