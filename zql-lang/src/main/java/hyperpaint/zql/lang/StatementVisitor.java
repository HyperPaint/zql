package hyperpaint.zql.lang;

import hyperpaint.zql.lang.antlr4.ZQLBaseVisitor;
import hyperpaint.zql.lang.antlr4.ZQLParser;
import hyperpaint.zql.lang.condition.Condition;
import hyperpaint.zql.lang.expression.Expression;
import hyperpaint.zql.lang.statement.Select;
import hyperpaint.zql.lang.statement.Statement;
import hyperpaint.zql.lang.znode.ZNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class StatementVisitor extends ZQLBaseVisitor<Statement> {
    static final StatementVisitor INSTANCE = new StatementVisitor();

    @Override
    public Statement visitStatement(ZQLParser.StatementContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public Statement visitSelect(ZQLParser.SelectContext ctx) {
        final Expression selectExpressions =  ctx.SELECT_WORD() != null ? ExpressionVisitor.INSTANCE.visit(ctx.selectExpressions()) : null;
        final ZNode fromZNodes = ctx.FROM_WORD() != null ? ZNodeVisitor.INSTANCE.visit(ctx.fromZnodes()) : null;
        final Condition whereConditions = ctx.WHERE_WORD() != null ? ConditionVisitor.INSTANCE.visit(ctx.whereConditions()) : null;
        final Expression groupByExpressions = ctx.GROUP_BY_WORD() != null ? ExpressionVisitor.INSTANCE.visit(ctx.groupByExpressions()) : null;
        final Condition havingConditions = ctx.HAVING_WORD() != null ? ConditionVisitor.INSTANCE.visit(ctx.havingConditions()) : null;
        final Expression orderByExpressions = ctx.ORDER_BY_WORD() != null ? ExpressionVisitor.INSTANCE.visit(ctx.orderByExpressions()) : null;

        return new Select(selectExpressions, fromZNodes, whereConditions, groupByExpressions, havingConditions, orderByExpressions);
    }
}
