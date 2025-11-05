package hyperpaint.zql.lang.condition;

import hyperpaint.zql.lang.base.Component;
import hyperpaint.zql.lang.base.ComponentEntryToValue;
import hyperpaint.zql.lang.base.ComponentRowToValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Condition extends Component<Condition> implements ComponentEntryToValue<Boolean>, ComponentRowToValue<Boolean> {
    public enum Type {
        AND,
        OR,

        BRACKETS,

        EQUALS,
        NOT_EQUALS,
        LIKE,
        NOT_LIKE
    }

    protected final Condition.Type type;

    @Override
    public Condition[] toComponents() {
        return new Condition[] { this };
    }
}
