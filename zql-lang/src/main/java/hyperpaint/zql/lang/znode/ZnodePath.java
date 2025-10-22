package hyperpaint.zql.lang.znode;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
public class ZnodePath extends Znode {
    private final String path;

    public ZnodePath(@NonNull Znode.Type type, @NonNull String path) {
        super(type);

        this.path = path;
    }
}
