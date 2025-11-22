package hyperpaint.zql.lang.expression;

import hyperpaint.zql.lang.Component;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Expression extends Component {
    public enum Type {
        COLLECTION,

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
}
