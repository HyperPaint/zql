package hyperpaint.zql.lang;

import hyperpaint.zql.lang.antlr4.ZQLBaseVisitor;
import hyperpaint.zql.lang.antlr4.ZQLParser;
import hyperpaint.zql.lang.condition.Condition;
import hyperpaint.zql.lang.condition.ConditionCompositeOfCondition;
import hyperpaint.zql.lang.condition.ConditionCompositeOfExpression;
import hyperpaint.zql.lang.condition.ConditionWrapper;
import hyperpaint.zql.lang.expression.Expression;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.tree.ParseTree;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ConditionVisitor extends ZQLBaseVisitor<Condition> {
    static final ConditionVisitor INSTANCE = new ConditionVisitor();

    private Condition and(ParseTree parseTree) {
        final Condition left = visit(parseTree.getChild(0));
        final Condition right = visit(parseTree.getChild(2));

        return new ConditionCompositeOfCondition(Condition.Type.C_C_AND, left, right);
    }

    private Condition or(ParseTree parseTree) {
        final Condition left = visit(parseTree.getChild(0));
        final Condition right = visit(parseTree.getChild(2));

        return new ConditionCompositeOfCondition(Condition.Type.C_C_OR, left, right);
    }

    private Condition brackets(ParseTree parseTree) {
        final Condition condition = visit(parseTree.getChild(1));

        return new ConditionWrapper(Condition.Type.C_C_WRAP, condition);
    }

    private Condition greater(ParseTree parseTree) {
        final Expression left = ExpressionVisitor.INSTANCE.visit(parseTree.getChild(0));
        final Expression right = ExpressionVisitor.INSTANCE.visit(parseTree.getChild(2));

        return new ConditionCompositeOfExpression(Condition.Type.E_E_GREATER, left, right);
    }

    private Condition greaterEquals(ParseTree parseTree) {
        final Expression left = ExpressionVisitor.INSTANCE.visit(parseTree.getChild(0));
        final Expression right = ExpressionVisitor.INSTANCE.visit(parseTree.getChild(2));

        return new ConditionCompositeOfExpression(Condition.Type.E_E_GREATER_EQUALS, left, right);
    }

    private Condition lower(ParseTree parseTree) {
        final Expression left = ExpressionVisitor.INSTANCE.visit(parseTree.getChild(0));
        final Expression right = ExpressionVisitor.INSTANCE.visit(parseTree.getChild(2));

        return new ConditionCompositeOfExpression(Condition.Type.E_E_LOWER, left, right);
    }

    private Condition lowerEquals(ParseTree parseTree) {
        final Expression left = ExpressionVisitor.INSTANCE.visit(parseTree.getChild(0));
        final Expression right = ExpressionVisitor.INSTANCE.visit(parseTree.getChild(2));

        return new ConditionCompositeOfExpression(Condition.Type.E_E_LOWER_EQUALS, left, right);
    }

    private Condition equals(ParseTree parseTree) {
        final Expression left = ExpressionVisitor.INSTANCE.visit(parseTree.getChild(0));
        final Expression right = ExpressionVisitor.INSTANCE.visit(parseTree.getChild(2));

        return new ConditionCompositeOfExpression(Condition.Type.E_E_EQUALS, left, right);
    }

    private Condition notEquals(ParseTree parseTree) {
        final Expression left = ExpressionVisitor.INSTANCE.visit(parseTree.getChild(0));
        final Expression right = ExpressionVisitor.INSTANCE.visit(parseTree.getChild(2));

        return new ConditionCompositeOfExpression(Condition.Type.E_E_NOT_EQUALS, left, right);
    }

    private Condition like(ParseTree parseTree) {
        final Expression left = ExpressionVisitor.INSTANCE.visit(parseTree.getChild(0));
        final Expression right = ExpressionVisitor.INSTANCE.visit(parseTree.getChild(2));

        return new ConditionCompositeOfExpression(Condition.Type.E_E_LIKE, left, right);
    }

    private Condition notLike(ParseTree parseTree) {
        final Expression left = ExpressionVisitor.INSTANCE.visit(parseTree.getChild(0));
        final Expression right = ExpressionVisitor.INSTANCE.visit(parseTree.getChild(2));

        return new ConditionCompositeOfExpression(Condition.Type.E_E_NOT_LIKE, left, right);
    }

    @Override
    public Condition visitWhereConditionsAnd(ZQLParser.WhereConditionsAndContext ctx) {
        return and(ctx);
    }

    @Override
    public Condition visitWhereConditionsOr(ZQLParser.WhereConditionsOrContext ctx) {
        return or(ctx);
    }

    @Override
    public Condition visitWhereConditionsBrackets(ZQLParser.WhereConditionsBracketsContext ctx) {
        return brackets(ctx);
    }

    @Override
    public Condition visitWhereConditionsBase(ZQLParser.WhereConditionsBaseContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public Condition visitWhereConditionGreater(ZQLParser.WhereConditionGreaterContext ctx) {
        return greater(ctx);
    }

    @Override
    public Condition visitWhereConditionGreaterEquals(ZQLParser.WhereConditionGreaterEqualsContext ctx) {
        return greaterEquals(ctx);
    }

    @Override
    public Condition visitWhereConditionLower(ZQLParser.WhereConditionLowerContext ctx) {
        return lower(ctx);
    }

    @Override
    public Condition visitWhereConditionLowerEquals(ZQLParser.WhereConditionLowerEqualsContext ctx) {
        return lowerEquals(ctx);
    }

    @Override
    public Condition visitWhereConditionEquals(ZQLParser.WhereConditionEqualsContext ctx) {
        return equals(ctx);
    }

    @Override
    public Condition visitWhereConditionNotEquals(ZQLParser.WhereConditionNotEqualsContext ctx) {
        return notEquals(ctx);
    }

    @Override
    public Condition visitWhereConditionLike(ZQLParser.WhereConditionLikeContext ctx) {
        return like(ctx);
    }

    @Override
    public Condition visitWhereConditionNotLike(ZQLParser.WhereConditionNotLikeContext ctx) {
        return notLike(ctx);
    }

    @Override
    public Condition visitHavingConditionsAnd(ZQLParser.HavingConditionsAndContext ctx) {
        return and(ctx);
    }

    @Override
    public Condition visitHavingConditionsOr(ZQLParser.HavingConditionsOrContext ctx) {
        return or(ctx);
    }

    @Override
    public Condition visitHavingConditionsBrackets(ZQLParser.HavingConditionsBracketsContext ctx) {
        return brackets(ctx);
    }

    @Override
    public Condition visitHavingConditionsBase(ZQLParser.HavingConditionsBaseContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public Condition visitHavingConditionGreater(ZQLParser.HavingConditionGreaterContext ctx) {
        return greater(ctx);
    }

    @Override
    public Condition visitHavingConditionGreaterEquals(ZQLParser.HavingConditionGreaterEqualsContext ctx) {
        return greaterEquals(ctx);
    }

    @Override
    public Condition visitHavingConditionLower(ZQLParser.HavingConditionLowerContext ctx) {
        return lower(ctx);
    }

    @Override
    public Condition visitHavingConditionLowerEquals(ZQLParser.HavingConditionLowerEqualsContext ctx) {
        return lowerEquals(ctx);
    }

    @Override
    public Condition visitHavingConditionEquals(ZQLParser.HavingConditionEqualsContext ctx) {
        return equals(ctx);
    }

    @Override
    public Condition visitHavingConditionNotEquals(ZQLParser.HavingConditionNotEqualsContext ctx) {
        return notEquals(ctx);
    }

    @Override
    public Condition visitHavingConditionLike(ZQLParser.HavingConditionLikeContext ctx) {
        return like(ctx);
    }

    @Override
    public Condition visitHavingConditionNotLike(ZQLParser.HavingConditionNotLikeContext ctx) {
        return notLike(ctx);
    }
}
