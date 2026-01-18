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
        ALIAS,

        E3A_SUBSTRING,

        E2A_JSON_PATH,
        E2A_CONCATENATION,
        E2A_ARITHMETICAL_PLUS,
        E2A_ARITHMETICAL_MINUS,
        E2A_ARITHMETICAL_MULTIPLY,
        E2A_ARITHMETICAL_DIVIDE,
        E1A_ARITHMETICAL_WRAP,

        G1A_COUNT,
        G1A_SUM,
        G1A_AVG,
        G1A_MIN,
        G1A_MAX,

        P_NUMBER,
        P_STRING,
        P_IDENTIFIER,

        ORDER_BY_ASC,
        ORDER_BY_DESC
    }

    protected final Expression.Type type;
}
