package hyperpaint.zql.condition;

import lombok.*;

@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractCondition {
    private final @NonNull ConditionType type;
}
