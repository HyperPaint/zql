package hyperpaint.zql.lang.expression;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

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
    public String text(boolean format) {
        return String.valueOf(number);
    }

    @Override
    public Number value(String path, String data) {
        return number;
    }
}
