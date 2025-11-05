package hyperpaint.zql.lang.condition;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.Map;

@Getter
@ToString(callSuper = true)
public class ConditionWrapper extends Condition {
    private final Condition wrappedCondition;

    public ConditionWrapper(@NonNull Type type, @NonNull Condition condition) {
        super(type);

        this.wrappedCondition = condition;
    }

    @Override
    public String toZql(boolean format) {
        return switch (type) {
            case BRACKETS -> "(" + wrappedCondition.toZql(format) + ")";
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        };
    }

    @Override
    public Boolean toValue(String path, String data) {
        return wrappedCondition.toValue(path, data);
    }

    @Override
    public Boolean toValue(Object[] row, Map<String, Integer> index) {
        return wrappedCondition.toValue(row, index);
    }
}
