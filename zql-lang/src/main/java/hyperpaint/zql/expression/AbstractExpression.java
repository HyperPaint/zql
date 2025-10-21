package hyperpaint.zql.expression;

import lombok.*;

@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractExpression {
    private final @NonNull ExpressionType type;
}
