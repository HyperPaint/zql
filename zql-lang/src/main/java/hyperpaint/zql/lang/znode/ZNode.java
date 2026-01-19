package hyperpaint.zql.lang.znode;

import hyperpaint.zql.lang.Component;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ZNode extends Component {
    public enum Type {
        COLLECTION,

        LIST,
        PATH
    }

    protected final Type type;

}
