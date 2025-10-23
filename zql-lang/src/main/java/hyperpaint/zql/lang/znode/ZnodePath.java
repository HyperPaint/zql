package hyperpaint.zql.lang.znode;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class ZnodePath extends Znode {
    private final String path;

    public ZnodePath(@NonNull String path) {
        super(Type.PATH);

        this.path = path;
    }
}
