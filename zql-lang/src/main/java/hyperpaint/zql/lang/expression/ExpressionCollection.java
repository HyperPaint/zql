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
    public String text(boolean format) {
        final StringBuilder result = new StringBuilder();

        if (format) {
            for (var iterator = list.iterator(); iterator.hasNext(); ) {
                result.append(iterator.next().text());

                if (iterator.hasNext()) {
                    result.append(",\n");
                }
            }
        } else {
            for (var iterator = list.iterator(); iterator.hasNext(); ) {
                result.append(iterator.next().text());

                if (iterator.hasNext()) {
                    result.append(", ");
                }
            }
        }

        return result.toString();
    }
}
