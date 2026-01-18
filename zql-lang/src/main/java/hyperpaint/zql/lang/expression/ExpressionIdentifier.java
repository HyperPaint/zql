package hyperpaint.zql.lang.expression;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class ExpressionIdentifier extends Expression {
    private final String identifier;

    public ExpressionIdentifier(@NonNull String identifier) {
        super(Type.P_IDENTIFIER);

        this.identifier = identifier;
    }

    @Override
    public String toZql(boolean formatted) {
        return identifier;
    }
}
