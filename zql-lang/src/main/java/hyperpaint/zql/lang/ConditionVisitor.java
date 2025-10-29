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

        return new ConditionCompositeOfCondition(Condition.Type.AND, left, right);
    }

    private Condition or(ParseTree parseTree) {
        final Condition left = visit(parseTree.getChild(0));
        final Condition right = visit(parseTree.getChild(2));

        return new ConditionCompositeOfCondition(Condition.Type.OR, left, right);
    }

    private Condition brackets(ParseTree parseTree) {
        final Condition condition = visit(parseTree.getChild(1));

        return new ConditionWrapper(Condition.Type.BRACKETS, condition);
    }

    private Condition equals(ParseTree parseTree) {
        final Expression left = ExpressionVisitor.INSTANCE.visit(parseTree.getChild(0));
        final Expression right = ExpressionVisitor.INSTANCE.visit(parseTree.getChild(2));

        return new ConditionCompositeOfExpression(Condition.Type.EQUALS, left, right);
    }

    private Condition notEquals(ParseTree parseTree) {
        final Expression left = ExpressionVisitor.INSTANCE.visit(parseTree.getChild(0));
        final Expression right = ExpressionVisitor.INSTANCE.visit(parseTree.getChild(2));

        return new ConditionCompositeOfExpression(Condition.Type.NOT_EQUALS, left, right);
    }

    private Condition like(ParseTree parseTree) {
        final Expression left = ExpressionVisitor.INSTANCE.visit(parseTree.getChild(0));
        final Expression right = ExpressionVisitor.INSTANCE.visit(parseTree.getChild(2));

        return new ConditionCompositeOfExpression(Condition.Type.LIKE, left, right);
    }

    private Condition notLike(ParseTree parseTree) {
        final Expression left = ExpressionVisitor.INSTANCE.visit(parseTree.getChild(0));
        final Expression right = ExpressionVisitor.INSTANCE.visit(parseTree.getChild(2));

        return new ConditionCompositeOfExpression(Condition.Type.NOT_LIKE, left, right);
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
