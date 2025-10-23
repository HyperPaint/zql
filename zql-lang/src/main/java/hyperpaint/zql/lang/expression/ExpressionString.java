package hyperpaint.zql.lang.expression;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class ExpressionString extends Expression {
    private final String text;

    public ExpressionString(@NonNull Type type, @NonNull String text) {
        super(type);

        this.text = text;
    }
}
