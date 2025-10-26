package hyperpaint.zql.lang.expression.select;

import hyperpaint.zql.lang.expression.Expression;
import lombok.*;

@Getter
@ToString(callSuper = true)
public class SelectExpressionWrapper extends Expression {
    private final Expression wrappedExpression;
    private final String alias;

    public SelectExpressionWrapper(@NonNull Expression expression, @NonNull String alias) {
        super(Type.ALIAS);

        this.wrappedExpression = expression;
        this.alias = alias;
    }

    @Override
    public String name() {
        return alias;
    }
}
