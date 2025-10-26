package hyperpaint.zql.lang;

import hyperpaint.zql.lang.antlr4.ZQLBaseVisitor;
import hyperpaint.zql.lang.antlr4.ZQLParser;
import hyperpaint.zql.lang.condition.Condition;
import hyperpaint.zql.lang.expression.Expression;
import hyperpaint.zql.lang.statement.Select;
import hyperpaint.zql.lang.statement.Statement;
import hyperpaint.zql.lang.znode.Znode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class StatementVisitor extends ZQLBaseVisitor<Statement> {
    static final StatementVisitor INSTANCE = new StatementVisitor();

    @Override
    public Statement visitSelect(ZQLParser.SelectContext ctx) {
        final Expression selectExpressions =  ctx.SELECT_WORD() != null ? ExpressionVisitor.INSTANCE.visit(ctx.select_expressions()) : null;
        final Znode fromZnodes = ctx.FROM_WORD() != null ? ZnodeVisitor.INSTANCE.visit(ctx.znodes()) : null;
        final Condition whereConditions = ctx.WHERE_WORD() != null ? ConditionVisitor.INSTANCE.visit(ctx.where_conditions()) : null;
        final Expression groupByExpressions = ctx.GROUP_BY_WORD() != null ? ExpressionVisitor.INSTANCE.visit(ctx.group_by_expressions()) : null;
        final Condition havingConditions = ctx.HAVING_WORD() != null ? ConditionVisitor.INSTANCE.visit(ctx.having_conditions()) : null;
        final Expression orderByExpressions = ctx.ORDER_BY_WORD() != null ? ExpressionVisitor.INSTANCE.visit(ctx.order_by_expressions()) : null;

        return new Select(selectExpressions, fromZnodes, whereConditions, groupByExpressions, havingConditions, orderByExpressions);
    }
}
