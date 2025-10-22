package hyperpaint.zql.lang;

import hyperpaint.zql.lang.antlr4.ZQLBaseVisitor;
import hyperpaint.zql.lang.antlr4.ZQLParser;
import hyperpaint.zql.lang.expression.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ExpressionVisitor extends ZQLBaseVisitor<Expression> {
    static final ExpressionVisitor INSTANCE = new ExpressionVisitor();

    @Override
    public Expression visitExpressionsCommaExpressions(ZQLParser.ExpressionsCommaExpressionsContext ctx) {
        final Expression left = visit(ctx.expressions(0));
        final Expression right = visit(ctx.expressions(1));

        if (left.getType() == Expression.Type.COMMA && right.getType() == Expression.Type.COMMA) {
            final ExpressionCollection leftCollection = (ExpressionCollection) left;
            final ExpressionCollection rightCollection = (ExpressionCollection) right;
            leftCollection.getCollection().addAll(rightCollection.getCollection());
            return leftCollection;
        } else if (left.getType() == Expression.Type.COMMA) {
            final ExpressionCollection leftCollection = (ExpressionCollection) left;
            leftCollection.getCollection().add(right);
            return leftCollection;
        } else if (right.getType() == Expression.Type.COMMA) {
            final ExpressionCollection rightCollection = (ExpressionCollection) right;
            rightCollection.getCollection().add(left);
            return rightCollection;
        } else {
            final List<Expression> list = new ArrayList<>();
            list.add(left);
            list.add(right);
            return new ExpressionCollection(Expression.Type.COMMA, list);
        }
    }

    @Override
    public Expression visitExpressionsBase(ZQLParser.ExpressionsBaseContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public Expression visitExpressionAsText(ZQLParser.ExpressionAsTextContext ctx) {
        final String payload = ctx.TEXT().getText().substring(1, ctx.TEXT().getText().length() - 1);
        return new ExpressionAlias(Expression.Type.ALIAS, visit(ctx.expression()), payload);
    }

    @Override
    public Expression visitExpressionAsIdentifier(ZQLParser.ExpressionAsIdentifierContext ctx) {
        final String payload = ctx.identifier().getText();
        return new ExpressionAlias(Expression.Type.ALIAS, visit(ctx.expression()), payload);
    }

    @Override
    public Expression visitExpressionCount(ZQLParser.ExpressionCountContext ctx) {
        return new ExpressionWrapper(Expression.Type.COUNT, visit(ctx.expression()));
    }

    @Override
    public Expression visitExpressionSum(ZQLParser.ExpressionSumContext ctx) {
        return new ExpressionWrapper(Expression.Type.SUM, visit(ctx.expression()));
    }

    @Override
    public Expression visitExpressionAvg(ZQLParser.ExpressionAvgContext ctx) {
        return new ExpressionWrapper(Expression.Type.AVG, visit(ctx.expression()));
    }

    @Override
    public Expression visitExpressionMin(ZQLParser.ExpressionMinContext ctx) {
        return new ExpressionWrapper(Expression.Type.MIN, visit(ctx.expression()));
    }

    @Override
    public Expression visitExpressionMax(ZQLParser.ExpressionMaxContext ctx) {
        return new ExpressionWrapper(Expression.Type.MAX, visit(ctx.expression()));
    }

    @Override
    public Expression visitExpressionJsonPath(ZQLParser.ExpressionJsonPathContext ctx) {
        return new ExpressionWrapper2(Expression.Type.JSON_PATH, visit(ctx.expression(0)), visit(ctx.expression(1)));
    }

    @Override
    public Expression visitExpressionText(ZQLParser.ExpressionTextContext ctx) {
        final String payload = ctx.TEXT().getText().substring(1, ctx.TEXT().getText().length() - 1);
        return new ExpressionString(Expression.Type.TEXT, payload);
    }

    @Override
    public Expression visitExpressionNumber(ZQLParser.ExpressionNumberContext ctx) {
        return new ExpressionNumber(Expression.Type.NUMBER, ctx.NUMBER().getText());
    }

    @Override
    public Expression visitExpressionIdentifier(ZQLParser.ExpressionIdentifierContext ctx) {
        return new ExpressionString(Expression.Type.IDENTIFIER, ctx.identifier().getText());
    }
}
