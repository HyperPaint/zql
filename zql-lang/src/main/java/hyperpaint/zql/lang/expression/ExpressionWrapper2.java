package hyperpaint.zql.lang.expression;

import com.jayway.jsonpath.JsonPath;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.Map;

@Getter
@ToString(callSuper = true)
public class ExpressionWrapper2 extends Expression {
    private final Expression wrappedExpression1;
    private final Expression wrappedExpression2;

    public ExpressionWrapper2(@NonNull Type type, @NonNull Expression expression1, @NonNull Expression expression2) {
        super(type);

        this.wrappedExpression1 = expression1;
        this.wrappedExpression2 = expression2;
    }

    @Override
    public String toZql(boolean formatted) {
        return switch (type) {
            case ALIAS -> wrappedExpression1.toZql(formatted) + " as " + wrappedExpression2.toZql(formatted);
            case JSON_PATH -> "json(" + wrappedExpression1.toZql(formatted) + ", " + wrappedExpression2.toZql(formatted) + ")";
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        };
    }

    @Override
    public Object toValue(String path, String data) {
        return switch (type) {
            case ALIAS -> wrappedExpression1.toValue(path, data);
            case JSON_PATH -> JsonPath.read(wrappedExpression1.toValue(path, data).toString(), wrappedExpression2.toValue(path, data).toString());
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        };
    }

    @Override
    public Object toValue(Object[] row, Map<String, Integer> index) {
        return switch (type) {
            case ALIAS -> wrappedExpression1.toValue(row, index);
            case JSON_PATH -> JsonPath.read(wrappedExpression1.toValue(row, index).toString(), wrappedExpression2.toValue(row, index).toString());
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        };
    }

    @Override
    public String toName() {
        return switch (type) {
            case ALIAS -> wrappedExpression1.toName();
            case JSON_PATH -> "json(" + wrappedExpression1.toName() + ", " + wrappedExpression2.toName() + ")";
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        };
    }

    @Override
    public boolean hasAlias() {
        return type == Type.ALIAS;
    }

    @Override
    public String toAlias() {
        return type == Type.ALIAS ? wrappedExpression2.toName() : null;
    }
}
