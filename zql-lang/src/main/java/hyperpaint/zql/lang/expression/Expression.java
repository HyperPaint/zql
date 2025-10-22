package hyperpaint.zql.lang.expression;

import lombok.*;

@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Expression {
    public enum Type {
        COMMA,
        ALIAS,
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

    private final Expression.Type type;
}
