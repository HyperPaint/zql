package hyperpaint.zql.lang.znode;

import lombok.*;

@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Znode {
    public enum Type {
        COMMA,

        LIST,

        PATH
    }

    protected final Type type;

    public final String text() {
        return text(false);
    }

    public String text(boolean format) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
