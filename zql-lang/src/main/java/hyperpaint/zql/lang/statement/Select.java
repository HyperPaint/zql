package hyperpaint.zql.lang.statement;

import hyperpaint.zql.lang.condition.Condition;
import hyperpaint.zql.lang.expression.Expression;
import hyperpaint.zql.lang.znode.Znode;
import lombok.Getter;
import lombok.ToString;

import java.util.stream.Collectors;

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

    @Override
    public String text(boolean format) {
        final StringBuilder result = new StringBuilder();

        if (hasSelectExpression()) {
            if (format) {
                result.append("select\n").append(selectExpression.text(format).lines().map(s -> "\t" + s).collect(Collectors.joining("\n"))).append("\n");
            } else {
                result.append("select ").append(selectExpression.text(format)).append(" ");
            }

        }

        if (hasFromZnode()) {
            if (format) {
                result.append("from\n").append(fromZnode.text(format).lines().map(s -> "\t" + s).collect(Collectors.joining("\n"))).append("\n");
            } else {
                result.append("from ").append(fromZnode.text(format)).append(" ");
            }
        }

        if (hasWhereCondition()) {
            if (format) {
                result.append("where\n").append(whereCondition.text(format).lines().map(s -> "\t" + s).collect(Collectors.joining("\n"))).append("\n");
            } else {
                result.append("where ").append(whereCondition.text(format)).append(" ");
            }
        }

        if (hasGroupByExpression()) {
            if (format) {
                result.append("group by\n").append(groupByExpression.text(format).lines().map(s -> "\t" + s).collect(Collectors.joining("\n"))).append("\n");
            } else {
                result.append("group by ").append(groupByExpression.text(format)).append(" ");
            }
        }

        if (hasHavingCondition()) {
            if (format) {
                result.append("having\n").append(havingCondition.text(format).lines().map(s -> "\t" + s).collect(Collectors.joining("\n"))).append("\n");
            } else {
                result.append("having ").append(havingCondition.text(format)).append(" ");
            }
        }

        if (hasOrderByExpression()) {
            if (format) {
                result.append("order by\n").append(orderByExpression.text(format).lines().map(s -> "\t" + s).collect(Collectors.joining("\n"))).append("\n");
            } else {
                result.append("order by ").append(orderByExpression.text(format)).append(" ");
            }
        }

        return result.toString().trim() + ";";
    }
}
