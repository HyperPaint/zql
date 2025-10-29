package hyperpaint.zql.lang;

import hyperpaint.zql.lang.antlr4.ZQLBaseVisitor;
import hyperpaint.zql.lang.antlr4.ZQLParser;
import hyperpaint.zql.lang.expression.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ExpressionVisitor extends ZQLBaseVisitor<Expression> {
    static final ExpressionVisitor INSTANCE = new ExpressionVisitor();

    private Expression visitExpressionsComma(ParseTree parseTree) {
        final Expression left = visit(parseTree.getChild(0));
        final Expression right = visit(parseTree.getChild(1));

        if (left.getType() == Expression.Type.COMMA && right.getType() == Expression.Type.COMMA) {
            final ExpressionCollection leftCollection = (ExpressionCollection) left;
            final ExpressionCollection rightCollection = (ExpressionCollection) right;
            leftCollection.getList().addAll(rightCollection.getList());
            return leftCollection;
        } else if (left.getType() == Expression.Type.COMMA) {
            final ExpressionCollection leftCollection = (ExpressionCollection) left;
            leftCollection.getList().add(right);
            return leftCollection;
        } else if (right.getType() == Expression.Type.COMMA) {
            final ExpressionCollection rightCollection = (ExpressionCollection) right;
            rightCollection.getList().add(left);
            return rightCollection;
        } else {
            final List<Expression> list = new ArrayList<>();
            list.add(left);
            list.add(right);
            return new ExpressionCollection(list);
        }
    }

    @Override
    public Expression visitSelectExpressionsComma(ZQLParser.SelectExpressionsCommaContext ctx) {
        return visitExpressionsComma(ctx);
    }

    @Override
    public Expression visitSelectExpressionsBase(ZQLParser.SelectExpressionsBaseContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public Expression visitSelectExpression(ZQLParser.SelectExpressionContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public Expression visitWhereExpressionLeft(ZQLParser.WhereExpressionLeftContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public Expression visitWhereExpressionRight(ZQLParser.WhereExpressionRightContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public Expression visitGroupByExpressionsComma(ZQLParser.GroupByExpressionsCommaContext ctx) {
        return visitExpressionsComma(ctx);
    }

    @Override
    public Expression visitGroupByExpressionsBase(ZQLParser.GroupByExpressionsBaseContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public Expression visitGroupByExpression(ZQLParser.GroupByExpressionContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public Expression visitHavingExpressionLeft(ZQLParser.HavingExpressionLeftContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public Expression visitHavingExpressionRight(ZQLParser.HavingExpressionRightContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public Expression visitOrderByExpressionsComma(ZQLParser.OrderByExpressionsCommaContext ctx) {
        return visitExpressionsComma(ctx);
    }

    @Override
    public Expression visitOrderByExpressionsBase(ZQLParser.OrderByExpressionsBaseContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public Expression visitOrderByExpression(ZQLParser.OrderByExpressionContext ctx) {
        if (ctx.ASC_WORD() != null) {
            return new ExpressionOrderBy(visit(ctx.getChild(0)), true);
        } else if (ctx.DESC_WORD() != null) {
            return new ExpressionOrderBy(visit(ctx.getChild(0)), false);
        } else {
            return new ExpressionOrderBy(visit(ctx.getChild(0)));
        }
    }

    @Override
    public Expression visitExpressionAlias(ZQLParser.ExpressionAliasContext ctx) {
        return new ExpressionWrapper2(Expression.Type.ALIAS, visit(ctx.getChild(0)), visit(ctx.getChild(1)));
    }

    @Override
    public Expression visitExpressionJsonPath(ZQLParser.ExpressionJsonPathContext ctx) {
        return new ExpressionWrapper2(Expression.Type.MAX, visit(ctx.getChild(0)), visit(ctx.getChild(1)));
    }

    @Override
    public Expression visitExpressionCount(ZQLParser.ExpressionCountContext ctx) {
        return new ExpressionWrapper(Expression.Type.COUNT, visit(ctx.getChild(0)));
    }

    @Override
    public Expression visitExpressionSum(ZQLParser.ExpressionSumContext ctx) {
        return new ExpressionWrapper(Expression.Type.SUM, visit(ctx.getChild(0)));
    }

    @Override
    public Expression visitExpressionAvg(ZQLParser.ExpressionAvgContext ctx) {
        return new ExpressionWrapper(Expression.Type.AVG, visit(ctx.getChild(0)));
    }

    @Override
    public Expression visitExpressionMin(ZQLParser.ExpressionMinContext ctx) {
        return new ExpressionWrapper(Expression.Type.MIN, visit(ctx.getChild(0)));
    }

    @Override
    public Expression visitExpressionMax(ZQLParser.ExpressionMaxContext ctx) {
        return new ExpressionWrapper(Expression.Type.MAX, visit(ctx.getChild(0)));
    }

    @Override
    public Expression visitExpressionNumber(ZQLParser.ExpressionNumberContext ctx) {
        final String payload = ctx.NUMBER().getText();
        return new ExpressionNumber(payload);
    }

    @Override
    public Expression visitExpressionString(ZQLParser.ExpressionStringContext ctx) {
        final String payload = ctx.STRING().getText().substring(1, ctx.STRING().getText().length() - 1);
        return new ExpressionString(payload);
    }

    @Override
    public Expression visitExpressionIdentifier(ZQLParser.ExpressionIdentifierContext ctx) {
        final String payload = ctx.identifier().getText();
        return new ExpressionIdentifier(payload);
    }
}
