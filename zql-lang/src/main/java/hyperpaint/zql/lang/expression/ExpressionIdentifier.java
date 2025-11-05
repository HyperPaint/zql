package hyperpaint.zql.lang.expression;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.Map;

@Getter
@ToString(callSuper = true)
public class ExpressionIdentifier extends Expression {
    private final String identifier;

    public ExpressionIdentifier(@NonNull String identifier) {
        super(Type.IDENTIFIER);

        this.identifier = identifier;
    }

    @Override
    public String toZql(boolean formatted) {
        return identifier;
    }

    @Override
    public Object toValue(String path, String data) {
        if (identifier.equalsIgnoreCase("path")) {
            return path;
        } else if (identifier.equalsIgnoreCase("data")) {
            if (data == null) {
                return null;
            }

            try {
                if (!data.contains(".")) {
                    return Integer.valueOf(data);
                } else {
                    return Float.valueOf(data);
                }
            } catch (NumberFormatException e) {
                return data;
            }
        } else {
            throw new IllegalArgumentException("Unexpected value: " + identifier);
        }
    }

    @Override
    public Object toValue(Object[] row, Map<String, Integer> index) {
        return row[index.get(identifier)];
    }

    @Override
    public String toName() {
        return identifier;
    }
}
