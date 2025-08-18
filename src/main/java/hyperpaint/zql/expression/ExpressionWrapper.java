package hyperpaint.zql.expression;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
public class ExpressionWrapper extends AbstractExpression {
    private final @NonNull AbstractExpression wrappedExpression;

    public ExpressionWrapper(ExpressionType type, @NonNull AbstractExpression baseExpression) {
        super(type);

        this.wrappedExpression = baseExpression;
    }
}
