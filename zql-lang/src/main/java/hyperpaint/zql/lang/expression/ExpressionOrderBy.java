package hyperpaint.zql.lang.expression;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class ExpressionOrderBy extends Expression {
    private final Expression wrappedExpression;
    private final boolean ascending;

    public ExpressionOrderBy(@NonNull Expression expression) {
        this(expression, true);
    }

    public ExpressionOrderBy(@NonNull Expression expression, boolean ascending) {
        super(Type.ORDER);

        this.wrappedExpression = expression;
        this.ascending = ascending;
    }

    public boolean isDescending() {
        return !ascending;
    }

    @Override
    public String text(boolean format) {
        if (ascending) {
            return wrappedExpression.text() + " asc";
        } else {
            return wrappedExpression.text() + " desc";
        }
    }
}
