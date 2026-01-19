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
        C_C_AND,
        C_C_OR,
        C_C_WRAP,

        E_E_GREATER,
        E_E_GREATER_EQUALS,
        E_E_LOWER,
        E_E_LOWER_EQUALS,
        E_E_EQUALS,
        E_E_NOT_EQUALS,
        E_E_LIKE,
        E_E_NOT_LIKE
    }

    protected final Condition.Type type;
}
