package hyperpaint.zql.lang.expression;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.Collection;

@Getter
@ToString
public class ExpressionCollection extends Expression {
    private final Collection<Expression> collection;

    public ExpressionCollection(@NonNull Type type, @NonNull Collection<Expression> collection) {
        super(type);

        this.collection = collection;
    }
}
