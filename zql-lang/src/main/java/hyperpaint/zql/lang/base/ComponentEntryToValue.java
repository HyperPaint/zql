package hyperpaint.zql.lang.base;

public interface ComponentEntryToValue<T> {
    T toValue(String path, String data);
}
