package hyperpaint.zql.condition;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
public class ConditionsWrapper extends AbstractCondition {
    private final @NonNull AbstractCondition wrappedConditions;

    public ConditionsWrapper(ConditionType type, @NonNull AbstractCondition baseConditions) {
        super(type);

        this.wrappedConditions = baseConditions;
    }
}
