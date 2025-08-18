package hyperpaint.zql.condition;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
public class ConditionsLogic extends AbstractCondition {
    private final @NonNull AbstractCondition left;
    private final @NonNull AbstractCondition right;

    public ConditionsLogic(ConditionType type, @NonNull AbstractCondition left, @NonNull AbstractCondition right) {
        super(type);

        this.left = left;
        this.right = right;
    }
}
