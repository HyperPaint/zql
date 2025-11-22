package hyperpaint.zql.lang.expression;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class ExpressionWrapper2 extends Expression {
    private final Expression wrappedExpression1;
    private final Expression wrappedExpression2;

    public ExpressionWrapper2(@NonNull Type type, @NonNull Expression expression1, @NonNull Expression expression2) {
        super(type);

        this.wrappedExpression1 = expression1;
        this.wrappedExpression2 = expression2;
    }

    @Override
    public String toZql(boolean formatted) {
        return switch (type) {
            case ALIAS -> wrappedExpression1.toZql(formatted) + " as " + wrappedExpression2.toZql(formatted);
            case JSON_PATH -> "json_path(" + wrappedExpression1.toZql(formatted) + ", " + wrappedExpression2.toZql(formatted) + ")";
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        };
    }
}
