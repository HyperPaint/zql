package hyperpaint.zql.statement;

import hyperpaint.zql.condition.AbstractCondition;
import hyperpaint.zql.expression.AbstractExpression;
import hyperpaint.zql.groups.AbstractGroup;
import hyperpaint.zql.znode.AbstractZnode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class Statement {
    private final AbstractExpression expressions;
    private final AbstractZnode znodes;
    private final AbstractCondition conditions;
    private final AbstractGroup groups;
}
