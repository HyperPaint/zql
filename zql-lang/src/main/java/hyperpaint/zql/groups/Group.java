package hyperpaint.zql.groups;

import hyperpaint.zql.expression.AbstractExpression;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
public class Group extends AbstractGroup {
    private final @NonNull AbstractExpression expression;

    public Group(GroupType type, @NonNull AbstractExpression expression) {
        super(type);

        this.expression = expression;
    }
}
