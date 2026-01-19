package hyperpaint.zql.exec.select;

import hyperpaint.zql.lang.expression.Expression;
import hyperpaint.zql.lang.expression.ExpressionWrapper;

public enum SortingTypes {
    NONE,
    ASCENDANT,
    DESCENDANT;

    public static SortingTypes from(Expression expression) {
        //noinspection SwitchStatementWithTooFewBranches
        return switch (expression) {
            case ExpressionWrapper expressionWrapper -> from(expressionWrapper.getType());
            default -> SortingTypes.NONE;
        };
    }

    private static SortingTypes from(Expression.Type type) {
        return switch (type) {
            case ORDER_BY_ASC -> SortingTypes.ASCENDANT;
            case ORDER_BY_DESC -> SortingTypes.DESCENDANT;
            default -> SortingTypes.NONE;
        };
    }

    public boolean isNotSorting() {
        return this == NONE;
    }

    public boolean isSorting() {
        return this != NONE;
    }
}
