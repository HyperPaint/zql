package hyperpaint.zql;

import hyperpaint.zql.antlr4.ZQLBaseVisitor;
import hyperpaint.zql.antlr4.ZQLParser;
import hyperpaint.zql.expression.AbstractExpression;
import hyperpaint.zql.expression.ExpressionList;
import hyperpaint.zql.expression.ExpressionType;

import java.util.ArrayList;
import java.util.List;

class ZqlExpressionsVisitor extends ZQLBaseVisitor<AbstractExpression> {
    static final ZqlExpressionsVisitor INSTANCE = new ZqlExpressionsVisitor();

    @Override
    public AbstractExpression visitExpressionsCommaExpressions(ZQLParser.ExpressionsCommaExpressionsContext ctx) {
        final AbstractExpression left = visit(ctx.expressions(0));
        final AbstractExpression right = visit(ctx.expressions(1));

        if (left.getType() == ExpressionType.COMMA && right.getType() == ExpressionType.COMMA) {
            final ExpressionList leftList = (ExpressionList) left;
            final ExpressionList rightList = (ExpressionList) right;
            leftList.getList().addAll(rightList.getList());
            return leftList;
        } else if (left.getType() == ExpressionType.COMMA) {
            final ExpressionList leftList = (ExpressionList) left;
            leftList.getList().add(right);
            return leftList;
        } else if (right.getType() == ExpressionType.COMMA) {
            final ExpressionList rightList = (ExpressionList) right;
            rightList.getList().add(left);
            return rightList;
        } else {
            final List<AbstractExpression> list = new ArrayList<>();
            list.add(left);
            list.add(right);
            return new ExpressionList(ExpressionType.COMMA, list);
        }
    }

    @Override
    public AbstractExpression visitExpressionsBase(ZQLParser.ExpressionsBaseContext ctx) {
        return ZqlExpressionVisitor.INSTANCE.visit(ctx.expression());
    }
}
