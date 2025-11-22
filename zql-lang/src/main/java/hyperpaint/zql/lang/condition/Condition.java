package hyperpaint.zql.lang.condition;

import hyperpaint.zql.lang.Component;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Condition extends Component {
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
}
