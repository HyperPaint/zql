package hyperpaint.zql.lang.expression;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class ExpressionNumber extends Expression {
    private final Number number;

    public ExpressionNumber(@NonNull String number) {
        super(Type.P_NUMBER);

        this.number = Float.valueOf(number);
    }

    @Override
    public String toZql(boolean formatted) {
        return String.valueOf(number);
    }
}
