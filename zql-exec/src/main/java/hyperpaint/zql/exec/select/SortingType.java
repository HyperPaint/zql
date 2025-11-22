package hyperpaint.zql.exec.select;

import hyperpaint.zql.lang.expression.Expression;
import hyperpaint.zql.lang.expression.ExpressionWrapper;

public enum SortingType {
    NONE,
    ASCENDANT,
    DESCENDANT;

    public static SortingType from(Expression expression) {
        //noinspection SwitchStatementWithTooFewBranches
        return switch (expression) {
            case ExpressionWrapper expressionWrapper -> from(expressionWrapper.getType());
            default -> SortingType.NONE;
        };
    }

    private static SortingType from(Expression.Type type) {
        return switch (type) {
            case ORDER_BY_ASC -> SortingType.ASCENDANT;
            case ORDER_BY_DESC -> SortingType.DESCENDANT;
            default -> SortingType.NONE;
        };
    }
}
