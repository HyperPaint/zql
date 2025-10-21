package hyperpaint.zql.expression;

import lombok.*;

@Getter
@ToString
public class ExpressionAlias extends AbstractExpression {
    private final @NonNull AbstractExpression wrappedExpression;
    private final @NonNull String alias;

    public ExpressionAlias(ExpressionType type, @NonNull AbstractExpression baseExpression, @NonNull String alias) {
        super(type);

        this.wrappedExpression = baseExpression;
        this.alias = alias;
    }
}
