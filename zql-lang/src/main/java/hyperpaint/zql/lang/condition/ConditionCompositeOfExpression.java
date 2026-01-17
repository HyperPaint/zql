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
            case GREATER -> left.toZql(formatted) + " > " + right.toZql(formatted);
            case GREATER_EQUALS -> left.toZql(formatted) + " >= " + right.toZql(formatted);
            case LOWER -> left.toZql(formatted) + " < " + right.toZql(formatted);
            case LOWER_EQUALS -> left.toZql(formatted) + " <= " + right.toZql(formatted);
            case EQUALS -> left.toZql(formatted) + " == " + right.toZql(formatted);
            case NOT_EQUALS -> left.toZql(formatted) + " != " + right.toZql(formatted);
            case LIKE -> left.toZql(formatted) + " =~ " + right.toZql(formatted);
            case NOT_LIKE -> left.toZql(formatted) + " !~ " + right.toZql(formatted);
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        };
    }
}
