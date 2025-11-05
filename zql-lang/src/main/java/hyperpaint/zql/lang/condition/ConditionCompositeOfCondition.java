package hyperpaint.zql.lang.condition;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.Map;

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
                case AND -> left.toZql(true) + "\nand " + right.toZql(true);
                case OR -> left.toZql(true) + "\nor " + right.toZql(true);
                default -> throw new IllegalArgumentException("Unexpected value: " + type);
            };
        } else {
            return switch (type) {
                case AND -> left.toZql(false) + " and " + right.toZql(false);
                case OR -> left.toZql(false) + " or " + right.toZql(false);
                default -> throw new IllegalArgumentException("Unexpected value: " + type);
            };
        }
    }

    @Override
    public Boolean toValue(String path, String data) {
        return switch (type) {
            case AND -> left.toValue(path, data) && right.toValue(path, data);
            case OR -> left.toValue(path, data) || right.toValue(path, data);
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        };
    }

    @Override
    public Boolean toValue(Object[] row, Map<String, Integer> index) {
        return switch (type) {
            case AND -> left.toValue(row, index) && right.toValue(row, index);
            case OR -> left.toValue(row, index) || right.toValue(row, index);
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        };
    }
}
