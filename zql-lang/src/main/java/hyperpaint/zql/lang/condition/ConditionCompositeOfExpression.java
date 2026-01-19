package hyperpaint.zql.lang.condition;

import hyperpaint.zql.lang.expression.Expression;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

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
            case E_E_GREATER -> left.toZql(formatted) + " > " + right.toZql(formatted);
            case E_E_GREATER_EQUALS -> left.toZql(formatted) + " >= " + right.toZql(formatted);
            case E_E_LOWER -> left.toZql(formatted) + " < " + right.toZql(formatted);
            case E_E_LOWER_EQUALS -> left.toZql(formatted) + " <= " + right.toZql(formatted);
            case E_E_EQUALS -> left.toZql(formatted) + " == " + right.toZql(formatted);
            case E_E_NOT_EQUALS -> left.toZql(formatted) + " != " + right.toZql(formatted);
            case E_E_LIKE -> left.toZql(formatted) + " =~ " + right.toZql(formatted);
            case E_E_NOT_LIKE -> left.toZql(formatted) + " !~ " + right.toZql(formatted);
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        };
    }
}
