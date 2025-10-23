package hyperpaint.zql.lang.znode;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.Collection;

@Getter
@ToString(callSuper = true)
public class ZnodeCollection extends Znode {
    private final Collection<Znode> collection;

    public ZnodeCollection(@NonNull Collection<Znode> collection) {
        super(Type.COMMA);

        this.collection = collection;
    }
}
