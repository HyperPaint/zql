package hyperpaint.zql.lang.expression.select;

import hyperpaint.zql.lang.expression.Expression;
import hyperpaint.zql.lang.expression.ExpressionCollection;
import lombok.NonNull;
import lombok.ToString;

import java.util.Collection;

@ToString(callSuper = true)
public class SelectExpressionCollection extends ExpressionCollection {
    public SelectExpressionCollection(@NonNull Collection<Expression> collection) {
        super(Type.COMMA, collection);
    }
}
