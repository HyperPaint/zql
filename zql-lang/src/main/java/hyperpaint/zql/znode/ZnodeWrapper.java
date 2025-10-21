package hyperpaint.zql.znode;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
public class ZnodeWrapper extends AbstractZnode {
    private final @NonNull AbstractZnode wrappedZnode;

    public ZnodeWrapper(ZnodeType type, @NonNull AbstractZnode baseZnode) {
        super(type);

        this.wrappedZnode = baseZnode;
    }
}
