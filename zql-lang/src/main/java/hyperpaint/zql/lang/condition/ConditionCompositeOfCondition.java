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
    public boolean value(String path, String data) {
        return switch (type) {
            case AND -> left.value(path, data) && right.value(path, data);
            case OR -> left.value(path, data) || right.value(path, data);
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        };
    }
}
