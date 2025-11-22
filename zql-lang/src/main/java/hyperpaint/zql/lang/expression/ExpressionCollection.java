package hyperpaint.zql.lang.expression;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.List;

@Getter
@ToString(callSuper = true)
public class ExpressionCollection extends Expression {
    private final List<Expression> list;

    public ExpressionCollection(@NonNull List<Expression> list) {
        super(Type.COMMA);

        this.list = list;
    }

    @Override
    public String toZql(boolean formatted) {
        final StringBuilder result = new StringBuilder();

        if (formatted) {
            for (var iterator = list.iterator(); iterator.hasNext(); ) {
                result.append(iterator.next().toZql(true));

                if (iterator.hasNext()) {
                    result.append(",\n");
                }
            }
        } else {
            for (var iterator = list.iterator(); iterator.hasNext(); ) {
                result.append(iterator.next().toZql(false));

                if (iterator.hasNext()) {
                    result.append(", ");
                }
            }
        }

        return result.toString();
    }
}
