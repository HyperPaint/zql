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
    public String toZql(boolean formatted) {
        return switch (type) {
            case E1A_ARITHMETICAL_WRAP -> "(" + wrappedExpression.toZql(formatted) + ")";
            case G1A_COUNT -> "count(" + wrappedExpression.toZql(formatted) + ")";
            case G1A_SUM -> "sum(" + wrappedExpression.toZql(formatted) + ")";
            case G1A_AVG -> "avg(" + wrappedExpression.toZql(formatted) + ")";
            case G1A_MIN -> "min(" + wrappedExpression.toZql(formatted) + ")";
            case G1A_MAX -> "max(" + wrappedExpression.toZql(formatted) + ")";
            case ORDER_BY_ASC -> wrappedExpression.toZql(formatted) + " asc";
            case ORDER_BY_DESC -> wrappedExpression.toZql(formatted) + " desc";
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        };
    }
}
