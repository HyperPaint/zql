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
    public String text(boolean format) {
        return switch (type) {
            case ALIAS -> wrappedExpression1.text() + " as " + wrappedExpression2.text();
            case JSON_PATH -> "json(" + wrappedExpression1.text() + ", " + wrappedExpression2.text() + ")";
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        };
    }
}
