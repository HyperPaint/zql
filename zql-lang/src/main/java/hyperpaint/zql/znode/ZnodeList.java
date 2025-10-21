package hyperpaint.zql.znode;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class ZnodeList extends AbstractZnode {
    private final @NonNull List<AbstractZnode> list;

    public ZnodeList(ZnodeType type, @NonNull List<AbstractZnode> list) {
        super(type);

        this.list = list;
    }
}
