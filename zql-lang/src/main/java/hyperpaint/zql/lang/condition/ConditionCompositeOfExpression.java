package hyperpaint.zql.lang.condition;

import hyperpaint.zql.lang.expression.Expression;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.Map;
import java.util.Objects;

@Getter
@ToString(callSuper = true)
public class ConditionCompositeOfExpression extends Condition {
    protected final Expression left;
    protected final Expression right;

    public ConditionCompositeOfExpression(@NonNull Type type, @NonNull Expression left, @NonNull Expression right) {
        super(type);

        this.left = left;
        this.right = right;
    }

    @Override
    public String toZql(boolean formatted) {
        return switch (type) {
            case EQUALS -> left.toZql(formatted) + " == " + right.toZql(formatted);
            case NOT_EQUALS -> left.toZql(formatted) + " != " + right.toZql(formatted);
            case LIKE -> left.toZql(formatted) + " =~ " + right.toZql(formatted);
            case NOT_LIKE -> left.toZql(formatted) + " !~ " + right.toZql(formatted);
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        };
    }

    @Override
    public Boolean toValue(String path, String data) {
        final Object o1 = left.toValue(path, data);
        final Object o2 = right.toValue(path, data);

        return switch (type) {
            case EQUALS -> Objects.equals(o1, o2);
            case NOT_EQUALS -> !Objects.equals(o1, o2);
            case LIKE -> o1.toString().matches(o2.toString());
            case NOT_LIKE -> !o1.toString().matches(o2.toString());
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        };
    }

    @Override
    public Boolean toValue(Object[] row, Map<String, Integer> index) {
        final Object o1 = left.toValue(row, index);
        final Object o2 = right.toValue(row, index);

        return switch (type) {
            case EQUALS -> Objects.equals(o1, o2);
            case NOT_EQUALS -> !Objects.equals(o1, o2);
            case LIKE -> o1.toString().matches(o2.toString());
            case NOT_LIKE -> !o1.toString().matches(o2.toString());
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        };
    }
}
