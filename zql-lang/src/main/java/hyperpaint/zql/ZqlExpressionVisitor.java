package hyperpaint.zql;

import hyperpaint.zql.antlr4.ZQLBaseVisitor;
import hyperpaint.zql.antlr4.ZQLParser;
import hyperpaint.zql.expression.*;

class ZqlExpressionVisitor extends ZQLBaseVisitor<AbstractExpression> {
    static final ZqlExpressionVisitor INSTANCE = new ZqlExpressionVisitor();

    @Override
    public AbstractExpression visitExpressionAlias(ZQLParser.ExpressionAliasContext ctx) {
        final AbstractExpression expression = visit(ctx.expression());

        if (expression.getType() == ExpressionType.ALIAS) {
            final int line = ctx.expression().getStart().getLine();
            final int column = ctx.expression().getStart().getStartIndex() + 1;
            throw new ZqlException("Expression must be not alias expression", line, column);
        }

        final String payload = ctx.TEXT().getText().substring(1, ctx.TEXT().getText().length() - 1);
        return new ExpressionAlias(ExpressionType.ALIAS, expression, payload);
    }

    @Override
    public AbstractExpression visitExpressionCount(ZQLParser.ExpressionCountContext ctx) {
        final AbstractExpression expression = visit(ctx.expression());

        if (expression.getType() != ExpressionType.PATH && expression.getType() != ExpressionType.DATA) {
            final int line = ctx.expression().getStart().getLine();
            final int column = ctx.expression().getStart().getStartIndex() + 1;
            throw new ZqlException("Expression must be path or data", line, column);
        }

        return new ExpressionWrapper(ExpressionType.COUNT, expression);
    }

    @Override
    public AbstractExpression visitExpressionSum(ZQLParser.ExpressionSumContext ctx) {
        final AbstractExpression expression = visit(ctx.expression());

        if (expression.getType() != ExpressionType.PATH && expression.getType() != ExpressionType.DATA) {
            final int line = ctx.expression().getStart().getLine();
            final int column = ctx.expression().getStart().getStartIndex() + 1;
            throw new ZqlException("Expression must be path or data", line, column);
        }

        return new ExpressionWrapper(ExpressionType.SUM, expression);
    }

    @Override
    public AbstractExpression visitExpressionAvg(ZQLParser.ExpressionAvgContext ctx) {
        final AbstractExpression expression = visit(ctx.expression());

        if (expression.getType() != ExpressionType.PATH && expression.getType() != ExpressionType.DATA) {
            final int line = ctx.expression().getStart().getLine();
            final int column = ctx.expression().getStart().getStartIndex() + 1;
            throw new ZqlException("Expression must be path or data", line, column);
        }

        return new ExpressionWrapper(ExpressionType.AVG, expression);
    }

    @Override
    public AbstractExpression visitExpressionMin(ZQLParser.ExpressionMinContext ctx) {
        final AbstractExpression expression = visit(ctx.expression());

        if (expression.getType() != ExpressionType.PATH && expression.getType() != ExpressionType.DATA) {
            final int line = ctx.expression().getStart().getLine();
            final int column = ctx.expression().getStart().getStartIndex() + 1;
            throw new ZqlException("Expression must be path or data", line, column);
        }

        return new ExpressionWrapper(ExpressionType.MIN, expression);
    }

    @Override
    public AbstractExpression visitExpressionMax(ZQLParser.ExpressionMaxContext ctx) {
        final AbstractExpression expression = visit(ctx.expression());

        if (expression.getType() != ExpressionType.PATH && expression.getType() != ExpressionType.DATA) {
            final int line = ctx.expression().getStart().getLine();
            final int column = ctx.expression().getStart().getStartIndex() + 1;
            throw new ZqlException("Expression must be path or data", line, column);
        }

        return new ExpressionWrapper(ExpressionType.MAX, expression);
    }

    @Override
    public AbstractExpression visitExpressionJson(ZQLParser.ExpressionJsonContext ctx) {
        return new Expression(ExpressionType.JSON, "");
    }

    @Override
    public AbstractExpression visitExpressionPath(ZQLParser.ExpressionPathContext ctx) {
        return new Expression(ExpressionType.PATH, ctx.PATH().getText());
    }

    @Override
    public AbstractExpression visitExpressionData(ZQLParser.ExpressionDataContext ctx) {
        return new Expression(ExpressionType.DATA, ctx.DATA().getText());
    }

    @Override
    public AbstractExpression visitExpressionText(ZQLParser.ExpressionTextContext ctx) {
        final String payload = ctx.TEXT().getText().substring(1, ctx.TEXT().getText().length() - 1);
        return new Expression(ExpressionType.TEXT, payload);
    }

    @Override
    public AbstractExpression visitExpressionNumber(ZQLParser.ExpressionNumberContext ctx) {
        return new Expression(ExpressionType.NUMBER, ctx.NUMBER().getText());
    }
}
