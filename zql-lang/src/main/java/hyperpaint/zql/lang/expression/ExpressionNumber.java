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

        if (number.contains(".") || number.contains(",")) {
            this.number = Float.valueOf(number);
        } else {
            this.number = Integer.valueOf(number);
        }
    }

    @Override
    public String toZql(boolean formatted) {
        return String.valueOf(number);
    }
}
