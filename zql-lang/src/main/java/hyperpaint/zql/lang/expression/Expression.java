package hyperpaint.zql.lang.expression;

import hyperpaint.zql.lang.base.Component;
import hyperpaint.zql.lang.base.ComponentRowToValue;
import hyperpaint.zql.lang.base.ComponentToName;
import hyperpaint.zql.lang.base.ComponentEntryToValue;
import lombok.*;

@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Expression extends Component<Expression> implements ComponentEntryToValue<Object>, ComponentRowToValue<Object>, ComponentToName {
    public enum Type {
        COMMA,

        COUNT,
        SUM,
        AVG,
        MIN,
        MAX,

        ALIAS,
        JSON_PATH,

        NUMBER,
        STRING,
        IDENTIFIER,

        ORDER_BY_ASC,
        ORDER_BY_DESC
    }

    protected final Expression.Type type;

    @Override
    public Expression[] toComponents() {
        return new Expression[] { this };
    }

    @Override
    public boolean hasAlias() {
        return false;
    }

    @Override
    public String toAlias() {
        return null;
    }
}
