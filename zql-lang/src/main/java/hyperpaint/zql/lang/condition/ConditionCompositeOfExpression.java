package hyperpaint.zql.lang.condition;

import hyperpaint.zql.lang.expression.Expression;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
public class ConditionCompositeOfExpression extends Condition {
    private final Expression left;
    private final Expression right;

    public ConditionCompositeOfExpression(@NonNull Type type, @NonNull Expression left, @NonNull Expression right) {
        super(type);

        this.left = left;
        this.right = right;
    }
}
