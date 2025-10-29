package hyperpaint.zql.lang.condition;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class ConditionWrapper extends Condition {
    private final Condition wrappedCondition;

    public ConditionWrapper(@NonNull Type type, @NonNull Condition condition) {
        super(type);

        this.wrappedCondition = condition;
    }

    @Override
    public boolean value(String path, String data) {
        return switch (type) {
            case BRACKETS -> wrappedCondition.value(path, data);
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        };
    }
}
