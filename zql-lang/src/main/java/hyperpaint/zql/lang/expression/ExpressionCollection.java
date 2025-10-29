package hyperpaint.zql.lang.expression;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.List;

@Getter
@ToString(callSuper = true)
public class ExpressionCollection extends Expression {
    private final List<Expression> list;

    public ExpressionCollection(@NonNull List<Expression> list) {
        super(Type.COMMA);

        this.list = list;
    }
}
