package hyperpaint.zql.lang.expression;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.List;
import java.util.Map;

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

    @Override
    public Expression[] toComponents() {
        return list.toArray(new Expression[0]);
    }

    @Override
    public Object toValue(String path, String data) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Object toValue(Object[] row, Map<String, Integer> index) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String toName() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
