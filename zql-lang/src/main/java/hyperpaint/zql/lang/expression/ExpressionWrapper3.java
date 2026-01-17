package hyperpaint.zql.lang.expression;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class ExpressionWrapper3 extends Expression {
    private final Expression wrappedExpression1;
    private final Expression wrappedExpression2;
    private final Expression wrappedExpression3;

    public ExpressionWrapper3(@NonNull Type type, @NonNull Expression expression1, @NonNull Expression expression2, @NonNull Expression expression3) {
        super(type);

        this.wrappedExpression1 = expression1;
        this.wrappedExpression2 = expression2;
        this.wrappedExpression3 = expression3;
    }

    @Override
    public String toZql(boolean formatted) {
        //noinspection SwitchStatementWithTooFewBranches
        return switch (type) {
            case SUBSTRING -> "substr(" + wrappedExpression1.toZql(formatted) + ", " + wrappedExpression2.toZql(formatted) + ", " + wrappedExpression3.toZql(formatted) + ")";
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        };
    }
}
