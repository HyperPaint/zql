package hyperpaint.zql.lang.znode;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class ZnodeList extends Znode {
    private final Znode wrappedZnode;

    public ZnodeList(@NonNull Znode znode) {
        super(Type.LIST);

        this.wrappedZnode = znode;
    }

    @Override
    public String toZql(boolean formatted) {
        return wrappedZnode.getType() == Type.LIST ? "ls/" + wrappedZnode.toZql(formatted) : "ls" + wrappedZnode.toZql(formatted);
    }
}
