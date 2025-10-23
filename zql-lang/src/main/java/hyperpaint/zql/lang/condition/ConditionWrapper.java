package hyperpaint.zql.lang.condition;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class ConditionWrapper extends Condition {
    private final @NonNull Condition wrappedCondition;

    public ConditionWrapper(@NonNull Type type, @NonNull Condition condition) {
        super(type);

        this.wrappedCondition = condition;
    }
}
