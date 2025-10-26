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

    @Override
    public String eval(String path, String data) {
        return switch (type) {
            case TEXT -> text;
            case IDENTIFIER -> {
                if (text.equalsIgnoreCase("path")) {
                    yield path;
                } else if (text.equalsIgnoreCase("data")) {
                    yield data;
                } else {
                    throw new IllegalArgumentException("Unexpected value: " + text);
                }
            }
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        };
    }

    @Override
    public String name() {
        return text;
    }
}
