package hyperpaint.zql.condition;

import hyperpaint.zql.expression.AbstractExpression;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
public class Condition extends AbstractCondition {
    private final @NonNull AbstractExpression left;
    private final @NonNull AbstractExpression right;

    public Condition(ConditionType type, @NonNull AbstractExpression left, @NonNull AbstractExpression right) {
        super(type);

        this.left = left;
        this.right = right;
    }
}
