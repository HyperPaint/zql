package hyperpaint.zql.lang.expression.order_by;

import hyperpaint.zql.lang.expression.Expression;
import hyperpaint.zql.lang.expression.ExpressionCollection;
import lombok.NonNull;
import lombok.ToString;

import java.util.Collection;

@ToString(callSuper = true)
public class OrderByExpressionCollection extends ExpressionCollection {
    public OrderByExpressionCollection(@NonNull Collection<Expression> collection) {
        super(Type.COMMA, collection);
    }
}
