package hyperpaint.zql.lang.base;

import java.util.Map;

public interface ComponentRowToValue<T> {
    T toValue(Object[] row, Map<String, Integer> index);
}
