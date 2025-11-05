package hyperpaint.zql.lang.expression;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.Map;

@Getter
@ToString(callSuper = true)
public class ExpressionWrapper extends Expression {
    private final Expression wrappedExpression;

    public ExpressionWrapper(@NonNull Type type, @NonNull Expression expression) {
        super(type);

        this.wrappedExpression = expression;
    }

    @Override
    public String toZql(boolean formatted) {
        return switch (type) {
            case COUNT -> "count(" + wrappedExpression.toZql(formatted) + ")";
            case SUM -> "sum(" + wrappedExpression.toZql(formatted) + ")";
            case AVG -> "avg(" + wrappedExpression.toZql(formatted) + ")";
            case MIN -> "min(" + wrappedExpression.toZql(formatted) + ")";
            case MAX -> "max(" + wrappedExpression.toZql(formatted) + ")";
            case ORDER_BY_ASC -> wrappedExpression.toZql(formatted) + " asc";
            case ORDER_BY_DESC -> wrappedExpression.toZql(formatted) + " desc";
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        };
    }

    @Override
    public Object toValue(String path, String data) {
        return switch (type) {
            case COUNT, SUM, AVG, MIN, MAX -> wrappedExpression.toValue(path, data);
            // case ORDER_BY_ASC
            // case ORDER_BY_DESC
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        };
    }

    @Override
    public Object toValue(Object[] row, Map<String, Integer> index) {
        return switch (type) {
            case COUNT, SUM, AVG, MIN, MAX -> wrappedExpression.toValue(row, index);
            // case ORDER_BY_ASC
            // case ORDER_BY_DESC
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        };
    }

    @Override
    public String toName() {
        return toZql(false);
    }
}
