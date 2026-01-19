package hyperpaint.zql.lang.statement;

import hyperpaint.zql.lang.Component;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Statement extends Component {
    public enum Type {
        SELECT
    }

    protected final Type type;

}
