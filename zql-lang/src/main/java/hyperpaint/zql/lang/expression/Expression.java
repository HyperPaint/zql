package hyperpaint.zql.lang.expression;

import lombok.*;

@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Expression {
    public enum Type {
        COMMA,

        ALIAS,
        ORDER,

        COUNT,
        SUM,
        AVG,
        MIN,
        MAX,
        JSON_PATH,

        TEXT,
        NUMBER,
        IDENTIFIER
    }

    protected final Expression.Type type;

    public Object eval(String path, String data) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public String name() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
