package hyperpaint.zql;

import hyperpaint.zql.antlr4.ZQLBaseVisitor;
import hyperpaint.zql.antlr4.ZQLParser;
import hyperpaint.zql.condition.AbstractCondition;
import hyperpaint.zql.condition.Condition;
import hyperpaint.zql.condition.ConditionType;
import hyperpaint.zql.expression.AbstractExpression;
import hyperpaint.zql.expression.ExpressionType;

class ZqlConditionVisitor extends ZQLBaseVisitor<AbstractCondition>  {
    static final ZqlConditionVisitor INSTANCE = new ZqlConditionVisitor();

    @Override
    public AbstractCondition visitConditionExpressionEqualsExpression(ZQLParser.ConditionExpressionEqualsExpressionContext ctx) {
        final AbstractExpression left = ZqlExpressionVisitor.INSTANCE.visit(ctx.expression(0));
        final AbstractExpression right = ZqlExpressionVisitor.INSTANCE.visit(ctx.expression(1));

        if (left.getType() != ExpressionType.JSON && left.getType() != ExpressionType.PATH && left.getType() != ExpressionType.DATA) {
            final int line = ctx.expression(0).getStart().getLine();
            final int column = ctx.expression(0).getStart().getStartIndex() + 1;
            throw new ZqlException("Left side of equals condition must be json, path or data", line, column);
        }

        if (right.getType() != ExpressionType.JSON && right.getType() != ExpressionType.TEXT && right.getType() != ExpressionType.NUMBER) {
            final int line = ctx.expression(1).getStart().getLine();
            final int column = ctx.expression(1).getStart().getStartIndex() + 1;
            throw new ZqlException("Right side of equals condition must be json, text or number", line, column);
        }

        return new Condition(ConditionType.EQUALS, left, right);
    }

    @Override
    public AbstractCondition visitConditionExpressionNotEqualsExpression(ZQLParser.ConditionExpressionNotEqualsExpressionContext ctx) {
        final AbstractExpression left = ZqlExpressionVisitor.INSTANCE.visit(ctx.expression(0));
        final AbstractExpression right = ZqlExpressionVisitor.INSTANCE.visit(ctx.expression(1));

        if (left.getType() != ExpressionType.JSON && left.getType() != ExpressionType.PATH && left.getType() != ExpressionType.DATA) {
            final int line = ctx.expression(0).getStart().getLine();
            final int column = ctx.expression(0).getStart().getStartIndex() + 1;
            throw new ZqlException("Left side of not equals condition must be json, path or data", line, column);
        }

        if (right.getType() != ExpressionType.JSON && right.getType() != ExpressionType.TEXT && right.getType() != ExpressionType.NUMBER) {
            final int line = ctx.expression(1).getStart().getLine();
            final int column = ctx.expression(1).getStart().getStartIndex() + 1;
            throw new ZqlException("Right side of not equals condition must be json, text or number", line, column);
        }

        return new Condition(ConditionType.NOT_EQUALS, left, right);
    }

    @Override
    public AbstractCondition visitConditionExpressionLikeExpression(ZQLParser.ConditionExpressionLikeExpressionContext ctx) {
        final AbstractExpression left = ZqlExpressionVisitor.INSTANCE.visit(ctx.expression(0));
        final AbstractExpression right = ZqlExpressionVisitor.INSTANCE.visit(ctx.expression(1));

        if (left.getType() != ExpressionType.JSON && left.getType() != ExpressionType.PATH && left.getType() != ExpressionType.DATA) {
            final int line = ctx.expression(0).getStart().getLine();
            final int column = ctx.expression(0).getStart().getStartIndex() + 1;
            throw new ZqlException("Left side of like condition must be json, path or data", line, column);
        }

        if (right.getType() != ExpressionType.JSON && right.getType() != ExpressionType.TEXT && right.getType() != ExpressionType.NUMBER) {
            final int line = ctx.expression(1).getStart().getLine();
            final int column = ctx.expression(1).getStart().getStartIndex() + 1;
            throw new ZqlException("Right side of like condition must be json, text or number", line, column);
        }

        return new Condition(ConditionType.LIKE, left, right);
    }

    @Override
    public AbstractCondition visitConditionExpressionNotLikeExpression(ZQLParser.ConditionExpressionNotLikeExpressionContext ctx) {
        final AbstractExpression left = ZqlExpressionVisitor.INSTANCE.visit(ctx.expression(0));
        final AbstractExpression right = ZqlExpressionVisitor.INSTANCE.visit(ctx.expression(1));

        if (left.getType() != ExpressionType.JSON && left.getType() != ExpressionType.PATH && left.getType() != ExpressionType.DATA) {
            final int line = ctx.expression(0).getStart().getLine();
            final int column = ctx.expression(0).getStart().getStartIndex() + 1;
            throw new ZqlException("Left side of not like condition must be json, path or data", line, column);
        }

        if (right.getType() != ExpressionType.JSON && right.getType() != ExpressionType.TEXT && right.getType() != ExpressionType.NUMBER) {
            final int line = ctx.expression(1).getStart().getLine();
            final int column = ctx.expression(1).getStart().getStartIndex() + 1;
            throw new ZqlException("Right side of not like condition must be json, text or number", line, column);
        }

        return new Condition(ConditionType.NOT_LIKE, left, right);
    }
}
