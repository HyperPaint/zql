package hyperpaint.zql.groups;

import lombok.*;

@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractGroup {
    private final @NonNull GroupType type;
}
