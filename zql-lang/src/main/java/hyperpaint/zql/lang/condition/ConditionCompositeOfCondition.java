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
    public String toZql(boolean formatted) {
        if (formatted) {
            return switch (type) {
                case C_C_AND -> left.toZql(true) + "\nand " + right.toZql(true);
                case C_C_OR -> left.toZql(true) + "\nor " + right.toZql(true);
                default -> throw new IllegalArgumentException("Unexpected value: " + type);
            };
        } else {
            return switch (type) {
                case C_C_AND -> left.toZql(false) + " and " + right.toZql(false);
                case C_C_OR -> left.toZql(false) + " or " + right.toZql(false);
                default -> throw new IllegalArgumentException("Unexpected value: " + type);
            };
        }
    }
}
