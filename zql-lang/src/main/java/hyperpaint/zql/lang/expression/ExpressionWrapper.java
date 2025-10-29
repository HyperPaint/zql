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
    public String name() {
        return switch (type) {
            case COUNT -> "count(" + wrappedExpression.name() + ")";
            case SUM -> "sum(" + wrappedExpression.name() + ")";
            case AVG -> "avg(" + wrappedExpression.name() + ")";
            case MIN -> "min(" + wrappedExpression.name() + ")";
            case MAX -> "max(" + wrappedExpression.name() + ")";
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        };
    }
}
