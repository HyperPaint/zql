package hyperpaint.zql.exec.select;

import hyperpaint.zql.lang.expression.Expression;
import hyperpaint.zql.lang.expression.ExpressionWrapper;
import hyperpaint.zql.lang.expression.ExpressionWrapper2;

public enum GroupingTypes {
    NONE,
    COUNT,
    SUM,
    AVG,
    MIN,
    MAX;

    public static GroupingTypes from(Expression expression) {
        return switch (expression) {
            case ExpressionWrapper expressionWrapper -> from(expressionWrapper.getType());
            case ExpressionWrapper2 expressionWrapper2 -> {
                if (expressionWrapper2.getType() == Expression.Type.ALIAS) {
                    yield from(expressionWrapper2.getWrappedExpression1().getType());
                } else {
                    yield GroupingTypes.NONE;
                }
            }
            default -> GroupingTypes.NONE;
        };
    }

    private static GroupingTypes from(Expression.Type type) {
        return switch (type) {
            case COUNT -> GroupingTypes.COUNT;
            case SUM -> GroupingTypes.SUM;
            case AVG -> GroupingTypes.AVG;
            case MIN -> GroupingTypes.MIN;
            case MAX -> GroupingTypes.MAX;
            default -> GroupingTypes.NONE;
        };
    }

    public boolean isNotGrouping() {
        return this == NONE;
    }

    public boolean isGrouping() {
        return this != NONE;
    }
}
