package hyperpaint.zql.lang.znode;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.Collection;

@Getter
@ToString
public class ZnodeCollection extends Znode {
    private final Collection<Znode> collection;

    public ZnodeCollection(@NonNull Znode.Type type, @NonNull Collection<Znode> collection) {
        super(type);

        this.collection = collection;
    }
}
