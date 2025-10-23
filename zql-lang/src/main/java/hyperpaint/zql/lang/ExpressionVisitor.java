package hyperpaint.zql.lang;

import hyperpaint.zql.lang.antlr4.ZQLBaseVisitor;
import hyperpaint.zql.lang.antlr4.ZQLParser;
import hyperpaint.zql.lang.expression.*;
import hyperpaint.zql.lang.expression.group_by.GroupByExpressionCollection;
import hyperpaint.zql.lang.expression.order_by.OrderByExpressionCollection;
import hyperpaint.zql.lang.expression.order_by.OrderByExpressionWrapper;
import hyperpaint.zql.lang.expression.select.SelectExpressionCollection;
import hyperpaint.zql.lang.expression.select.SelectExpressionWrapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ExpressionVisitor extends ZQLBaseVisitor<Expression> {
    static final ExpressionVisitor INSTANCE = new ExpressionVisitor();

    // region Select

    @Override
    public Expression visitSelectExpressionsCommaExpressions(ZQLParser.SelectExpressionsCommaExpressionsContext ctx) {
        final Expression left = visit(ctx.select_expressions(0));
        final Expression right = visit(ctx.select_expressions(1));

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
            return new SelectExpressionCollection(list);
        }
    }

    @Override
    public Expression visitSelectExpressionsBase(ZQLParser.SelectExpressionsBaseContext ctx) {
        return visit(ctx.select_expression());
    }

    @Override
    public Expression visitSelectExpressionAlias(ZQLParser.SelectExpressionAliasContext ctx) {
        return visit(ctx.expression_alias());
    }

    @Override
    public Expression visitSelectExpressionFunction(ZQLParser.SelectExpressionFunctionContext ctx) {
        return visit(ctx.expression_function());
    }

    @Override
    public Expression visitSelectExpressionPrimitive(ZQLParser.SelectExpressionPrimitiveContext ctx) {
        return visit(ctx.expression_primitive());
    }

    // endregion

    // region Condition

    @Override
    public Expression visitConditionExpressionFunction(ZQLParser.ConditionExpressionFunctionContext ctx) {
        return visit(ctx.expression_function());
    }

    @Override
    public Expression visitConditionExpressionPrimitive(ZQLParser.ConditionExpressionPrimitiveContext ctx) {
        return visit(ctx.expression_primitive());
    }

    // endregion

    // region Group by

    @Override
    public Expression visitGroupByExpressionsCommaGroupByExpressions(ZQLParser.GroupByExpressionsCommaGroupByExpressionsContext ctx) {
        final Expression left = visit(ctx.group_by_expressions(0));
        final Expression right = visit(ctx.group_by_expressions(1));

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
            return new GroupByExpressionCollection(list);
        }
    }

    @Override
    public Expression visitGroupByExpressionsBase(ZQLParser.GroupByExpressionsBaseContext ctx) {
        return visit(ctx.group_by_expression());
    }

    @Override
    public Expression visitGroupByExpressionFunction(ZQLParser.GroupByExpressionFunctionContext ctx) {
        return visit(ctx.expression_function());
    }

    @Override
    public Expression visitGroupByExpressionPrimitive(ZQLParser.GroupByExpressionPrimitiveContext ctx) {
        return visit(ctx.expression_primitive());
    }

    // endregion

    // region Order by

    @Override
    public Expression visitOrderByExpressionsCommaOrderByExpressions(ZQLParser.OrderByExpressionsCommaOrderByExpressionsContext ctx) {
        final Expression left = visit(ctx.order_by_expressions(0));
        final Expression right = visit(ctx.order_by_expressions(1));

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
            return new OrderByExpressionCollection(list);
        }
    }

    @Override
    public Expression visitOrderByExpressionsBase(ZQLParser.OrderByExpressionsBaseContext ctx) {
        if (ctx.ASC_WORD() != null) {
            return new OrderByExpressionWrapper(visit(ctx.order_by_expression()), true);
        } else if (ctx.DESC_WORD() != null) {
            return new OrderByExpressionWrapper(visit(ctx.order_by_expression()), false);
        } else {
            return new OrderByExpressionWrapper(visit(ctx.order_by_expression()));
        }
    }

    @Override
    public Expression visitOrderByExpressionFunction(ZQLParser.OrderByExpressionFunctionContext ctx) {
        return visit(ctx.expression_function());
    }

    @Override
    public Expression visitOrderByExpressionPrimitive(ZQLParser.OrderByExpressionPrimitiveContext ctx) {
        return visit(ctx.expression_primitive());
    }

    // endregion

    // region Expression alias

    @Override
    public Expression visitExpressionAsText(ZQLParser.ExpressionAsTextContext ctx) {
        final String payload = ctx.TEXT().getText().substring(1, ctx.TEXT().getText().length() - 1);

        if (ctx.expression_function() != null) {
            return new SelectExpressionWrapper(visit(ctx.expression_function()), payload);
        } else if (ctx.expression_primitive() != null) {
            return new SelectExpressionWrapper(visit(ctx.expression_primitive()), payload);
        } else {
            throw new ZQLException("Expression function and expression primitive are missing in context");
        }
    }

    @Override
    public Expression visitExpressionAsIdentifier(ZQLParser.ExpressionAsIdentifierContext ctx) {
        final String payload = ctx.identifier().getText();

        if (ctx.expression_function() != null) {
            return new SelectExpressionWrapper(visit(ctx.expression_function()), payload);
        } else if (ctx.expression_primitive() != null) {
            return new SelectExpressionWrapper(visit(ctx.expression_primitive()), payload);
        } else {
            throw new ZQLException("Expression function and expression primitive are missing in context");
        }
    }

    // endregion

    // region Expression function

    @Override
    public Expression visitExpressionCount(ZQLParser.ExpressionCountContext ctx) {
        if (ctx.expression_function() != null) {
            return new ExpressionWrapper(Expression.Type.COUNT, visit(ctx.expression_function()));
        } else if (ctx.expression_primitive() != null) {
            return new ExpressionWrapper(Expression.Type.COUNT, visit(ctx.expression_primitive()));
        } else {
            throw new ZQLException("Expression function and expression primitive are missing in context");
        }
    }

    @Override
    public Expression visitExpressionSum(ZQLParser.ExpressionSumContext ctx) {
        if (ctx.expression_function() != null) {
            return new ExpressionWrapper(Expression.Type.SUM, visit(ctx.expression_function()));
        } else if (ctx.expression_primitive() != null) {
            return new ExpressionWrapper(Expression.Type.SUM, visit(ctx.expression_primitive()));
        } else {
            throw new ZQLException("Expression function and expression primitive are missing in context");
        }
    }

    @Override
    public Expression visitExpressionAvg(ZQLParser.ExpressionAvgContext ctx) {
        if (ctx.expression_function() != null) {
            return new ExpressionWrapper(Expression.Type.AVG, visit(ctx.expression_function()));
        } else if (ctx.expression_primitive() != null) {
            return new ExpressionWrapper(Expression.Type.AVG, visit(ctx.expression_primitive()));
        } else {
            throw new ZQLException("Expression function and expression primitive are missing in context");
        }
    }

    @Override
    public Expression visitExpressionMin(ZQLParser.ExpressionMinContext ctx) {
        if (ctx.expression_function() != null) {
            return new ExpressionWrapper(Expression.Type.MIN, visit(ctx.expression_function()));
        } else if (ctx.expression_primitive() != null) {
            return new ExpressionWrapper(Expression.Type.MIN, visit(ctx.expression_primitive()));
        } else {
            throw new ZQLException("Expression function and expression primitive are missing in context");
        }
    }

    @Override
    public Expression visitExpressionMax(ZQLParser.ExpressionMaxContext ctx) {
        if (ctx.expression_function() != null) {
            return new ExpressionWrapper(Expression.Type.MAX, visit(ctx.expression_function()));
        } else if (ctx.expression_primitive() != null) {
            return new ExpressionWrapper(Expression.Type.MAX, visit(ctx.expression_primitive()));
        } else {
            throw new ZQLException("Expression function and expression primitive are missing in context");
        }
    }

    @Override
    public Expression visitExpressionJsonPath(ZQLParser.ExpressionJsonPathContext ctx) {
        if (ctx.expression_function(0) != null) {
            if (ctx.expression_function(1) != null) {
                return new ExpressionWrapper2(Expression.Type.JSON_PATH, visit(ctx.expression_function(0)), visit(ctx.expression_function(1)));
            } else if (ctx.expression_primitive(1) != null) {
                return new ExpressionWrapper2(Expression.Type.JSON_PATH, visit(ctx.expression_function(0)), visit(ctx.expression_primitive(1)));
            } else {
                throw new ZQLException("Expression function and expression primitive are missing in context");
            }
        } else if (ctx.expression_primitive(0) != null) {
            if (ctx.expression_function(1) != null) {
                return new ExpressionWrapper2(Expression.Type.JSON_PATH, visit(ctx.expression_primitive(0)), visit(ctx.expression_function(1)));
            } else if (ctx.expression_primitive(1) != null) {
                return new ExpressionWrapper2(Expression.Type.JSON_PATH, visit(ctx.expression_primitive(0)), visit(ctx.expression_primitive(1)));
            } else {
                throw new ZQLException("Expression function and expression primitive are missing in context");
            }
        } else {
            throw new ZQLException("Expression function and expression primitive are missing in context");
        }
    }

    // endregion

    // region Expression primitive

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

    // endregion
}
