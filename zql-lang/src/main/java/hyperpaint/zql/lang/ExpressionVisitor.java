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
        final Expression right = visit(parseTree.getChild(2));

        if (left.getType() == Expression.Type.COLLECTION && right.getType() == Expression.Type.COLLECTION) {
            final ExpressionCollection leftCollection = (ExpressionCollection) left;
            final ExpressionCollection rightCollection = (ExpressionCollection) right;
            leftCollection.getList().addAll(rightCollection.getList());
            return leftCollection;
        } else if (left.getType() == Expression.Type.COLLECTION) {
            final ExpressionCollection leftCollection = (ExpressionCollection) left;
            leftCollection.getList().add(right);
            return leftCollection;
        } else if (right.getType() == Expression.Type.COLLECTION) {
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
        return visit(ctx.selectExpression());
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
        return visit(ctx.groupByExpression());
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
        return visit(ctx.orderByExpression());
    }

    @Override
    public Expression visitOrderByExpression(ZQLParser.OrderByExpressionContext ctx) {
        if (ctx.DESC_WORD() == null) {
            return new ExpressionWrapper(Expression.Type.ORDER_BY_ASC, visit(ctx.getChild(0)));
        } else {
            return new ExpressionWrapper(Expression.Type.ORDER_BY_DESC, visit(ctx.getChild(0)));
        }
    }

    @Override
    public Expression visitExpressionAlias(ZQLParser.ExpressionAliasContext ctx) {
        if (ctx.AS_WORD() != null) {
            return new ExpressionWrapper2(Expression.Type.ALIAS, visit(ctx.getChild(0)), visit(ctx.getChild(2)));
        } else {
            return new ExpressionWrapper2(Expression.Type.ALIAS, visit(ctx.getChild(0)), visit(ctx.getChild(1)));
        }
    }

    @Override
    public Expression visitExpressionJsonPath(ZQLParser.ExpressionJsonPathContext ctx) {
        return new ExpressionWrapper2(Expression.Type.JSON_PATH, visit(ctx.getChild(2)), visit(ctx.getChild(4)));
    }

    @Override
    public Expression visitExpressionSubstring(ZQLParser.ExpressionSubstringContext ctx) {
        return new ExpressionWrapper3(Expression.Type.SUBSTRING, visit(ctx.getChild(2)), visit(ctx.getChild(4)), visit(ctx.getChild(6)));
    }

    @Override
    public Expression visitExpressionCount(ZQLParser.ExpressionCountContext ctx) {
        return new ExpressionWrapper(Expression.Type.COUNT, visit(ctx.getChild(2)));
    }

    @Override
    public Expression visitExpressionSum(ZQLParser.ExpressionSumContext ctx) {
        return new ExpressionWrapper(Expression.Type.SUM, visit(ctx.getChild(2)));
    }

    @Override
    public Expression visitExpressionAvg(ZQLParser.ExpressionAvgContext ctx) {
        return new ExpressionWrapper(Expression.Type.AVG, visit(ctx.getChild(2)));
    }

    @Override
    public Expression visitExpressionMin(ZQLParser.ExpressionMinContext ctx) {
        return new ExpressionWrapper(Expression.Type.MIN, visit(ctx.getChild(2)));
    }

    @Override
    public Expression visitExpressionMax(ZQLParser.ExpressionMaxContext ctx) {
        return new ExpressionWrapper(Expression.Type.MAX, visit(ctx.getChild(2)));
    }

    @Override
    public Expression visitExpressionArithmeticalPlus(ZQLParser.ExpressionArithmeticalPlusContext ctx) {
        return new ExpressionWrapper2(Expression.Type.ARITHMETICAL_PLUS, visit(ctx.getChild(0)), visit(ctx.getChild(2)));
    }

    @Override
    public Expression visitExpressionArithmeticalMinus(ZQLParser.ExpressionArithmeticalMinusContext ctx) {
        return new ExpressionWrapper2(Expression.Type.ARITHMETICAL_MINUS, visit(ctx.getChild(0)), visit(ctx.getChild(2)));
    }

    @Override
    public Expression visitExpressionArithmeticalMultiply(ZQLParser.ExpressionArithmeticalMultiplyContext ctx) {
        return new ExpressionWrapper2(Expression.Type.ARITHMETICAL_MULTIPLY, visit(ctx.getChild(0)), visit(ctx.getChild(2)));
    }

    @Override
    public Expression visitExpressionArithmeticalDiv(ZQLParser.ExpressionArithmeticalDivContext ctx) {
        return new ExpressionWrapper2(Expression.Type.ARITHMETICAL_DIV, visit(ctx.getChild(0)), visit(ctx.getChild(2)));
    }

    @Override
    public Expression visitExpressionArithmeticalBrackets(ZQLParser.ExpressionArithmeticalBracketsContext ctx) {
        return new ExpressionWrapper(Expression.Type.ARITHMETICAL_BRACKETS, visit(ctx.getChild(1)));
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
