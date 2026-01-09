package hyperpaint.zql.lang.statement;

import hyperpaint.zql.lang.condition.Condition;
import hyperpaint.zql.lang.expression.Expression;
import hyperpaint.zql.lang.znode.ZNode;
import lombok.Getter;
import lombok.ToString;

import java.util.stream.Collectors;

@Getter
@ToString
public class Select extends Statement {
    private final Expression selectExpression;
    private final ZNode fromZnode;
    private final Condition whereCondition;
    private final Expression groupByExpression;
    private final Condition havingCondition;
    private final Expression orderByExpression;

    public Select(
            Expression selectExpression,
            ZNode fromZnode,
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

    @Override
    public String toZql(boolean formatted) {
        final StringBuilder result = new StringBuilder();

        if (hasSelectExpression()) {
            if (formatted) {
                result.append("select\n").append(selectExpression.toZql(true).lines().map(s -> "\t" + s).collect(Collectors.joining("\n"))).append("\n");
            } else {
                result.append("select ").append(selectExpression.toZql(false)).append(" ");
            }

        }

        if (hasFromZnode()) {
            if (formatted) {
                result.append("from\n").append(fromZnode.toZql(true).lines().map(s -> "\t" + s).collect(Collectors.joining("\n"))).append("\n");
            } else {
                result.append("from ").append(fromZnode.toZql(false)).append(" ");
            }
        }

        if (hasWhereCondition()) {
            if (formatted) {
                result.append("where\n").append(whereCondition.toZql(true).lines().map(s -> "\t" + s).collect(Collectors.joining("\n"))).append("\n");
            } else {
                result.append("where ").append(whereCondition.toZql(false)).append(" ");
            }
        }

        if (hasGroupByExpression()) {
            if (formatted) {
                result.append("group by\n").append(groupByExpression.toZql(true).lines().map(s -> "\t" + s).collect(Collectors.joining("\n"))).append("\n");
            } else {
                result.append("group by ").append(groupByExpression.toZql(false)).append(" ");
            }
        }

        if (hasHavingCondition()) {
            if (formatted) {
                result.append("having\n").append(havingCondition.toZql(true).lines().map(s -> "\t" + s).collect(Collectors.joining("\n"))).append("\n");
            } else {
                result.append("having ").append(havingCondition.toZql(false)).append(" ");
            }
        }

        if (hasOrderByExpression()) {
            if (formatted) {
                result.append("order by\n").append(orderByExpression.toZql(true).lines().map(s -> "\t" + s).collect(Collectors.joining("\n"))).append("\n");
            } else {
                result.append("order by ").append(orderByExpression.toZql(false)).append(" ");
            }
        }

        return result.toString().trim() + ";";
    }
}
