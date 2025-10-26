package hyperpaint.zql.lang.expression.select;

import hyperpaint.zql.lang.expression.Expression;
import hyperpaint.zql.lang.expression.ExpressionCollection;
import lombok.NonNull;
import lombok.ToString;

import java.util.List;

@ToString(callSuper = true)
public class SelectExpressionCollection extends ExpressionCollection {
    public SelectExpressionCollection(@NonNull List<Expression> list) {
        super(Type.COMMA, list);
    }
}
