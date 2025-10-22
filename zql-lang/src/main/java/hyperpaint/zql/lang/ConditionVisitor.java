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

    @Override
    public Condition visitConditionsAndConditions(ZQLParser.ConditionsAndConditionsContext ctx) {
        final Condition left = visit(ctx.conditions(0));
        final Condition right = visit(ctx.conditions(1));

        return new ConditionCompositeOfCondition(Condition.Type.AND, left, right);
    }

    @Override
    public Condition visitConditionsOrConditions(ZQLParser.ConditionsOrConditionsContext ctx) {
        final Condition left = visit(ctx.conditions(0));
        final Condition right = visit(ctx.conditions(1));

        return new ConditionCompositeOfCondition(Condition.Type.OR, left, right);
    }

    @Override
    public Condition visitConditionsInBrackets(ZQLParser.ConditionsInBracketsContext ctx) {
        return new ConditionWrapper(Condition.Type.IN_BRACKETS, visit(ctx.conditions()));
    }

    @Override
    public Condition visitConditionsBase(ZQLParser.ConditionsBaseContext ctx) {
        return visit(ctx.condition());
    }

    @Override
    public Condition visitConditionEquals(ZQLParser.ConditionEqualsContext ctx) {
        final Expression left = ExpressionVisitor.INSTANCE.visit(ctx.expression(0));
        final Expression right = ExpressionVisitor.INSTANCE.visit(ctx.expression(1));

        return new ConditionCompositeOfExpression(Condition.Type.EQUALS, left, right);
    }

    @Override
    public Condition visitConditionNotEquals(ZQLParser.ConditionNotEqualsContext ctx) {
        final Expression left = ExpressionVisitor.INSTANCE.visit(ctx.expression(0));
        final Expression right = ExpressionVisitor.INSTANCE.visit(ctx.expression(1));

        return new ConditionCompositeOfExpression(Condition.Type.NOT_EQUALS, left, right);
    }

    @Override
    public Condition visitConditionLike(ZQLParser.ConditionLikeContext ctx) {
        final Expression left = ExpressionVisitor.INSTANCE.visit(ctx.expression(0));
        final Expression right = ExpressionVisitor.INSTANCE.visit(ctx.expression(1));

        return new ConditionCompositeOfExpression(Condition.Type.LIKE, left, right);
    }

    @Override
    public Condition visitConditionNotLike(ZQLParser.ConditionNotLikeContext ctx) {
        final Expression left = ExpressionVisitor.INSTANCE.visit(ctx.expression(0));
        final Expression right = ExpressionVisitor.INSTANCE.visit(ctx.expression(1));

        return new ConditionCompositeOfExpression(Condition.Type.NOT_LIKE, left, right);
    }
}
