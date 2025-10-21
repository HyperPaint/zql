package hyperpaint.zql;

import hyperpaint.zql.antlr4.ZQLBaseVisitor;
import hyperpaint.zql.antlr4.ZQLParser;
import hyperpaint.zql.expression.AbstractExpression;
import hyperpaint.zql.expression.ExpressionType;
import hyperpaint.zql.groups.AbstractGroup;
import hyperpaint.zql.groups.Group;
import hyperpaint.zql.groups.GroupType;

class ZqlGroupVisitor extends ZQLBaseVisitor<AbstractGroup> {
    static final ZqlGroupVisitor INSTANCE = new ZqlGroupVisitor();

    @Override
    public AbstractGroup visitGroupExpression(ZQLParser.GroupExpressionContext ctx) {
        final AbstractExpression expression = ZqlExpressionVisitor.INSTANCE.visit(ctx.expression());

        if (expression.getType() != ExpressionType.JSON
                && expression.getType() != ExpressionType.PATH
                && expression.getType() != ExpressionType.DATA
                && expression.getType() != ExpressionType.TEXT
                && expression.getType() != ExpressionType.NUMBER) {
            final int line = ctx.expression().getStart().getLine();
            final int column = ctx.expression().getStart().getStartIndex() + 1;
            throw new ZqlException("Group must be json, path, data, text or number", line, column);
        }

        return new Group(GroupType.GROUP, expression);
    }
}
