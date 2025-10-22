package hyperpaint.zql.lang.condition;

import lombok.*;

@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Condition {
    public enum Type {
        AND,
        OR,
        IN_BRACKETS,
        EQUALS,
        NOT_EQUALS,
        LIKE,
        NOT_LIKE
    }

    private final Condition.Type type;
}
