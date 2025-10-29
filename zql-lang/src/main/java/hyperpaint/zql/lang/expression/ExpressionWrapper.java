package hyperpaint.zql.lang.expression;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class ExpressionWrapper extends Expression {
    private final Expression wrappedExpression;

    public ExpressionWrapper(@NonNull Type type, @NonNull Expression expression) {
        super(type);

        this.wrappedExpression = expression;
    }

    @Override
    public String text(boolean format) {
        return switch (type) {
            case COUNT -> "count(" + wrappedExpression.text() + ")";
            case SUM -> "sum(" + wrappedExpression.text() + ")";
            case AVG -> "avg(" + wrappedExpression.text() + ")";
            case MIN -> "min(" + wrappedExpression.text() + ")";
            case MAX -> "max(" + wrappedExpression.text() + ")";
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        };
    }
}
