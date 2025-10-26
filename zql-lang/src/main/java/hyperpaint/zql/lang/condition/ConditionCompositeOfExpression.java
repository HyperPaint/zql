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

    public ConditionCompositeOfExpression(Type type, Expression left, Expression right) {
        super(type);

        this.left = left;
        this.right = right;
    }

    @Override
    public boolean pass(String path, String data) {
        return switch (getType()) {
            case EQUALS -> Objects.equals(left.eval(path, data).toString(), right.eval(path, data).toString());
            case NOT_EQUALS -> !Objects.equals(left.eval(path, data).toString(), right.eval(path, data).toString());
            case LIKE -> left.eval(path, data).toString().matches(right.eval(path, data).toString());
            case NOT_LIKE -> !left.eval(path, data).toString().matches(right.eval(path, data).toString());
            default -> throw new IllegalArgumentException("Unexpected value: " + getType());
        };
    }
}
