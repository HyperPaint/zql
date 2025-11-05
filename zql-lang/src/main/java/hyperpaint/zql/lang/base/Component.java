package hyperpaint.zql.lang.base;

public abstract class Component<T> {
    public final String toZql() {
        return toZql(false);
    }

    public final String toFormattedZql() {
        return toZql(true);
    }

    public abstract String toZql(boolean formatted);

    public abstract T[] toComponents();
}
