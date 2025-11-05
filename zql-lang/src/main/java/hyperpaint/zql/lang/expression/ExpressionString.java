package hyperpaint.zql.lang.expression;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.Map;

@Getter
@ToString(callSuper = true)
public class ExpressionString extends Expression {
    private final String string;

    public ExpressionString(@NonNull String string) {
        super(Type.STRING);

        this.string = string;
    }

    @Override
    public String toZql(boolean formatted) {
        if (string.contains("\"")) {
            return "'" + string + "'";
        } else {
            return "\"" + string + "\"";
        }
    }

    @Override
    public Object toValue(String path, String data) {
        return string;
    }

    @Override
    public Object toValue(Object[] row, Map<String, Integer> index) {
        return string;
    }

    @Override
    public String toName() {
        return string;
    }
}
