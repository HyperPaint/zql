package hyperpaint.zql.lang.znode;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class ZNodePath extends ZNode {
    private final String path;

    public ZNodePath(@NonNull String path) {
        super(Type.PATH);

        this.path = path;
    }

    @Override
    public String toZql(boolean formatted) {
        return path;
    }
}
