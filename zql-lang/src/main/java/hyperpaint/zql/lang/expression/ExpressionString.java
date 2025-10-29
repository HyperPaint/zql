package hyperpaint.zql.lang.expression;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class ExpressionString extends Expression {
    private final String string;

    public ExpressionString(@NonNull String string) {
        super(Type.STRING);

        this.string = string;
    }

    @Override
    public String name() {
        return string;
    }

    @Override
    public String value(String path, String data) {
        return string;
    }
}
