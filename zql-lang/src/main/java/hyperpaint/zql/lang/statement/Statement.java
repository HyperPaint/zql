package hyperpaint.zql.lang.statement;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Statement {
    public enum Type {
        SELECT
    }

    protected final Type type;

    public final String text() {
        return text(false);
    }

    public String text(boolean format) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
