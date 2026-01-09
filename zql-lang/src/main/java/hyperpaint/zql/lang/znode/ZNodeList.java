package hyperpaint.zql.lang.znode;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class ZNodeList extends ZNode {
    private final ZNode wrappedZnode;

    public ZNodeList(@NonNull ZNode znode) {
        super(Type.LIST);

        this.wrappedZnode = znode;
    }

    @Override
    public String toZql(boolean formatted) {
        return wrappedZnode.getType() == Type.LIST ? "ls/" + wrappedZnode.toZql(formatted) : "ls" + wrappedZnode.toZql(formatted);
    }
}
