package hyperpaint.zql.lang.statement;

import hyperpaint.zql.lang.condition.Condition;
import hyperpaint.zql.lang.expression.Expression;
import hyperpaint.zql.lang.znode.Znode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Select extends Statement {
    private final Expression selectExpression;
    private final Znode fromZnode;
    private final Condition whereCondition;
    private final Expression groupByExpression;
    private final Condition havingCondition;
    private final Expression orderByExpression;

    public Select(
            Expression selectExpression,
            Znode fromZnode,
            Condition whereCondition,
            Expression groupByExpression,
            Condition havingCondition,
            Expression orderByExpression
    ) {
        super(Type.SELECT);

        this.selectExpression = selectExpression;
        this.fromZnode = fromZnode;
        this.whereCondition = whereCondition;
        this.groupByExpression = groupByExpression;
        this.havingCondition = havingCondition;
        this.orderByExpression = orderByExpression;
    }

    public boolean hasSelectExpression() {
        return selectExpression != null;
    }

    public boolean hasFromZnode() {
        return fromZnode != null;
    }

    public boolean hasWhereCondition() {
        return whereCondition != null;
    }

    public boolean hasGroupByExpression() {
        return groupByExpression != null;
    }

    public boolean hasHavingCondition() {
        return havingCondition != null;
    }

    public boolean hasOrderByExpression() {
        return orderByExpression != null;
    }
}
