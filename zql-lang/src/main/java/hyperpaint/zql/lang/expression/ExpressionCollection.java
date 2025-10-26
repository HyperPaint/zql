package hyperpaint.zql.lang.expression;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.List;

@Getter
@ToString(callSuper = true)
public abstract class ExpressionCollection extends Expression {
    private final List<Expression> list;

    public ExpressionCollection(Type type, List<Expression> list) {
        super(type);

        this.list = list;
    }
}
