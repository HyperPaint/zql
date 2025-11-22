package hyperpaint.zql.lang;

public abstract class Component {
    public final String toZql() {
        return toZql(false);
    }

    public final String toFormattedZql() {
        return toZql(true);
    }

    public abstract String toZql(boolean formatted);
}
