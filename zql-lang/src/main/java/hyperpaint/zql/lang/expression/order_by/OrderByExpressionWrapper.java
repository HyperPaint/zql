package hyperpaint.zql.lang.expression.order_by;

import hyperpaint.zql.lang.expression.Expression;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class OrderByExpressionWrapper extends Expression {
    private final Expression wrappedExpression;
    private final boolean ascending;

    public OrderByExpressionWrapper(@NonNull Expression expression) {
        super(Type.ORDER);

        this.wrappedExpression = expression;
        this.ascending = true;
    }

    public OrderByExpressionWrapper(@NonNull Expression expression, boolean ascending) {
        super(Type.ORDER);

        this.wrappedExpression = expression;
        this.ascending = ascending;
    }

    public boolean isDescending() {
        return !ascending;
    }
}
