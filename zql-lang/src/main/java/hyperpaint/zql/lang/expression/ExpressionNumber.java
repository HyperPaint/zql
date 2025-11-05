package hyperpaint.zql.lang.expression;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.Map;

@Getter
@ToString(callSuper = true)
public class ExpressionNumber extends Expression {
    private final Number number;

    public ExpressionNumber(@NonNull String number) {
        super(Type.NUMBER);

        if (!number.contains(".")) {
            this.number = Integer.valueOf(number);
        } else {
            this.number = Float.valueOf(number);
        }
    }

    boolean isInteger() {
        return number instanceof Integer;
    }

    boolean isFloat() {
        return number instanceof Float;
    }

    @Override
    public String toZql(boolean formatted) {
        return String.valueOf(number);
    }

    @Override
    public Object toValue(String path, String data) {
        return number;
    }

    @Override
    public Object toValue(Object[] row, Map<String, Integer> index) {
        return number;
    }

    @Override
    public String toName() {
        return String.valueOf(number);
    }
}
