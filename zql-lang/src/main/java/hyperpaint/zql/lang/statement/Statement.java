package hyperpaint.zql.lang.statement;

import hyperpaint.zql.lang.base.Component;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Statement extends Component<Statement> {
    public enum Type {
        SELECT
    }

    protected final Type type;

    @Override
    public Statement[] toComponents() {
        return new Statement[] { this };
    }
}
