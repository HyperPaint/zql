package hyperpaint.zql.lang.expression.group_by;

import hyperpaint.zql.lang.expression.Expression;
import hyperpaint.zql.lang.expression.ExpressionCollection;
import lombok.NonNull;
import lombok.ToString;

import java.util.Collection;

@ToString(callSuper = true)
public class GroupByExpressionCollection extends ExpressionCollection {
    public GroupByExpressionCollection(@NonNull Collection<Expression> collection) {
        super(Type.COMMA, collection);
    }
}
