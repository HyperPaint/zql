package hyperpaint.zql.lang.expression;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
public class ExpressionNumber extends Expression {
    private final Number number;

    public ExpressionNumber(@NonNull Type type, @NonNull String number) {
        super(type);

        if (number.contains(".")) {
            this.number = Float.valueOf(number);
        } else {
            this.number = Integer.valueOf(number);
        }
    }
}
