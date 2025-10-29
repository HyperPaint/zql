package hyperpaint.zql.lang.expression;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class ExpressionIdentifier extends Expression {
    private final String identifier;

    public ExpressionIdentifier(@NonNull String identifier) {
        super(Type.IDENTIFIER);

        this.identifier = identifier;
    }

    @Override
    public String name() {
        return identifier;
    }

    @Override
    public String value(String path, String data) throws IllegalArgumentException {
        if (identifier.equalsIgnoreCase("path")) {
            return path;
        } else if (identifier.equalsIgnoreCase("data")) {
            return data;
        } else {
            throw new IllegalArgumentException("Unexpected value: " + identifier);
        }
    }
}
