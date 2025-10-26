package hyperpaint.zql.lang.condition;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class ConditionCompositeOfCondition extends Condition {
    private final Condition left;
    private final Condition right;

    public ConditionCompositeOfCondition(@NonNull Type type, @NonNull Condition left, @NonNull Condition right) {
        super(type);

        this.left = left;
        this.right = right;
    }

    @Override
    public boolean pass(String path, String data) {
        return switch (getType()) {
            case AND -> left.pass(path, data) && right.pass(path, data);
            case OR -> left.pass(path, data) || right.pass(path, data);
            default -> throw new IllegalArgumentException("Unexpected value: " + getType());
        };
    }
}
