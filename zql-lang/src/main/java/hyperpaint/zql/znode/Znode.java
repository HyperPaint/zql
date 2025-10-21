package hyperpaint.zql.znode;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
public class Znode extends AbstractZnode {
    private final @NonNull String path;

    public Znode(ZnodeType type, @NonNull String path) {
        super(type);

        this.path = path;
    }
}
