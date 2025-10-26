package hyperpaint.zql.lang.expression.group_by;

import hyperpaint.zql.lang.expression.Expression;
import hyperpaint.zql.lang.expression.ExpressionCollection;
import lombok.NonNull;
import lombok.ToString;

import java.util.List;

@ToString(callSuper = true)
public class GroupByExpressionCollection extends ExpressionCollection {
    public GroupByExpressionCollection(@NonNull List<Expression> list) {
        super(Type.COMMA, list);
    }
}
