package hyperpaint.zql.lang.expression;

import lombok.*;

@Getter
@ToString
public class ExpressionAlias extends Expression {
    private final Expression wrappedExpression;
    private final String alias;

    public ExpressionAlias(@NonNull Type type, @NonNull Expression expression, @NonNull String alias) {
        super(type);

        this.wrappedExpression = expression;
        this.alias = alias;
    }
}
