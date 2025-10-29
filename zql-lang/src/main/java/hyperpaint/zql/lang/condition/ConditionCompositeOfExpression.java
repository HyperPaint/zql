package hyperpaint.zql.lang.condition;

import hyperpaint.zql.lang.expression.Expression;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

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
    public String text(boolean format) {
        return switch (type) {
            case EQUALS -> left.text() + " == " + right.text();
            case NOT_EQUALS -> left.text() + " != " + right.text();
            case LIKE -> left.text() + " =~ " + right.text();
            case NOT_LIKE -> left.text() + " !~ " + right.text();
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        };
    }

    @Override
    public boolean value(String path, String data) {
        return switch (type) {
            case EQUALS -> Objects.equals(left.value(path, data).toString(), right.value(path, data).toString());
            case NOT_EQUALS -> !Objects.equals(left.value(path, data).toString(), right.value(path, data).toString());
            case LIKE -> left.value(path, data).toString().matches(right.value(path, data).toString());
            case NOT_LIKE -> !left.value(path, data).toString().matches(right.value(path, data).toString());
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        };
    }
}
