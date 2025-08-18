package hyperpaint.zql.expression;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class ExpressionList extends AbstractExpression {
    private final @NonNull List<AbstractExpression> list;

    public ExpressionList(ExpressionType type, @NonNull List<AbstractExpression> list) {
        super(type);

        this.list = list;
    }
}
