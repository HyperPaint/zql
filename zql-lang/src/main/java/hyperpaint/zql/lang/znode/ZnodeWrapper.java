package hyperpaint.zql.lang.znode;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class ZnodeWrapper extends Znode {
    private final Znode wrappedZnode;

    public ZnodeWrapper(@NonNull Znode.Type type, @NonNull Znode znode) {
        super(type);

        this.wrappedZnode = znode;
    }

    @Override
    public String toZql(boolean formatted) {
        return "ls/" + wrappedZnode.toZql(formatted);
    }
}
