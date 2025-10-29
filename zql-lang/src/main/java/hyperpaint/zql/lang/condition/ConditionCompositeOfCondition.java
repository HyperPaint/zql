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
    public String text(boolean format) {
        if (format) {
            return switch (type) {
                case AND -> left.text() + "\nand " + right.text();
                case OR -> left.text() + "\nor " + right.text();
                default -> throw new IllegalArgumentException("Unexpected value: " + type);
            };
        } else {
            return switch (type) {
                case AND -> left.text() + " and " + right.text();
                case OR -> left.text() + " or " + right.text();
                default -> throw new IllegalArgumentException("Unexpected value: " + type);
            };
        }
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
