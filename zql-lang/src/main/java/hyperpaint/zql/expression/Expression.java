package hyperpaint.zql.expression;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
public class Expression extends AbstractExpression {
    private final @NonNull String text;

    public Expression(ExpressionType type, @NonNull String text) {
        super(type);

        this.text = text;
    }
}
