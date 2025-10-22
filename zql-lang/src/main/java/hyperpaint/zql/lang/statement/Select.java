package hyperpaint.zql.lang.statement;

import hyperpaint.zql.lang.condition.ConditionCompositeOfCondition;
import hyperpaint.zql.lang.expression.ExpressionCollection;
import hyperpaint.zql.lang.znode.ZnodeCollection;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
public class Select extends Statement {
    private final ExpressionCollection selectExpressions;
    private final ZnodeCollection fromZnodes;
    private final ConditionCompositeOfCondition whereConditions;
    private final ExpressionCollection groupByExpressions;
    private final ConditionCompositeOfCondition havingConditions;
    private final ExpressionCollection orderByExpressions;

    public Select(
            @NonNull Statement.Type type,
            ExpressionCollection selectExpressions,
            ZnodeCollection fromZnodes,
            ConditionCompositeOfCondition whereConditions,
            ExpressionCollection groupByExpressions,
            ConditionCompositeOfCondition havingConditions,
            ExpressionCollection orderByExpressions
    ) {
        super(type);

        this.selectExpressions = selectExpressions;
        this.fromZnodes = fromZnodes;
        this.whereConditions = whereConditions;
        this.groupByExpressions = groupByExpressions;
        this.havingConditions = havingConditions;
        this.orderByExpressions = orderByExpressions;
    }
}
