package hyperpaint.zql;

import hyperpaint.zql.antlr4.ZQLBaseVisitor;
import hyperpaint.zql.antlr4.ZQLParser;
import hyperpaint.zql.condition.*;

class ZqlConditionsVisitor extends ZQLBaseVisitor<AbstractCondition> {
    static final ZqlConditionsVisitor INSTANCE = new ZqlConditionsVisitor();

    @Override
    public AbstractCondition visitConditionsAndConditions(ZQLParser.ConditionsAndConditionsContext ctx) {
        return new ConditionsLogic(ConditionType.AND, visit(ctx.conditions(0)), visit(ctx.conditions(1)));
    }

    @Override
    public AbstractCondition visitConditionsOrConditions(ZQLParser.ConditionsOrConditionsContext ctx) {
        return new ConditionsLogic(ConditionType.OR, visit(ctx.conditions(0)), visit(ctx.conditions(1)));
    }

    @Override
    public AbstractCondition visitConditionsInBrackets(ZQLParser.ConditionsInBracketsContext ctx) {
        return new ConditionsWrapper(ConditionType.IN_BRACKETS, visit(ctx.conditions()));
    }

    @Override
    public AbstractCondition visitConditionsBase(ZQLParser.ConditionsBaseContext ctx) {
        return ZqlConditionVisitor.INSTANCE.visit(ctx.condition());
    }
}
