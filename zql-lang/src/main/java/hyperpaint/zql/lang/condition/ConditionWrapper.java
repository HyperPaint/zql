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
    public String toZql(boolean formatted) {
        //noinspection SwitchStatementWithTooFewBranches
        return switch (type) {
            case C_C_WRAP -> "(" + wrappedCondition.toZql(formatted) + ")";
            default -> throw new IllegalArgumentException("Unexpected value: " + type);
        };
    }
}
