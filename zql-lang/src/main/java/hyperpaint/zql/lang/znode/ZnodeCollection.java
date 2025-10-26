package hyperpaint.zql.lang.znode;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.List;

@Getter
@ToString(callSuper = true)
public class ZnodeCollection extends Znode {
    private final List<Znode> list;

    public ZnodeCollection(@NonNull List<Znode> list) {
        super(Type.COMMA);

        this.list = list;
    }
}
