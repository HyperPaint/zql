package hyperpaint.zql.lang.expression;

import lombok.*;

@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Expression {
    public enum Type {
        COMMA,

        JSON_PATH,
        ALIAS,

        COUNT,
        SUM,
        AVG,
        MIN,
        MAX,

        NUMBER,
        STRING,
        IDENTIFIER,

        ORDER
    }

    protected final Expression.Type type;

    public String name() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Object value(String path, String data) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
