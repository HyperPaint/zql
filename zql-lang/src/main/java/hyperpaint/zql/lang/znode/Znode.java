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

    private final Type type;
}
