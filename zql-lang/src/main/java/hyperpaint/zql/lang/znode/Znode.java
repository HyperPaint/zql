package hyperpaint.zql.lang.znode;

import hyperpaint.zql.lang.base.Component;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Znode extends Component<Znode> {
    public enum Type {
        COMMA,

        LIST,

        PATH
    }

    protected final Type type;

    @Override
    public Znode[] toComponents() {
        return new Znode[] { this };
    }
}
