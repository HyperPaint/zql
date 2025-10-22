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

    private final Type type;
}
