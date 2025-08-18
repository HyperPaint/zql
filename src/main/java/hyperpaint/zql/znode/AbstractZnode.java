package hyperpaint.zql.znode;

import lombok.*;

@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractZnode {
    private final @NonNull ZnodeType type;
}
