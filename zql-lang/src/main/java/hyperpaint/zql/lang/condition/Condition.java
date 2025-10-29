package hyperpaint.zql.lang.condition;

import lombok.*;

@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Condition {
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

    public final String text() {
        return text(false);
    }

    public String text(boolean format) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean value(String path, String data) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
