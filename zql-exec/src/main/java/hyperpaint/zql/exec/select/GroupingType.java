package hyperpaint.zql.exec.select;

import hyperpaint.zql.lang.expression.Expression;
import hyperpaint.zql.lang.expression.ExpressionWrapper;
import hyperpaint.zql.lang.expression.ExpressionWrapper2;

public enum GroupingType {
    NONE,
    COUNT,
    SUM,
    AVG,
    MIN,
    MAX;

    public static GroupingType from(Expression expression) {
        return switch (expression) {
            case ExpressionWrapper expressionWrapper -> from(expressionWrapper.getType());
            case ExpressionWrapper2 expressionWrapper2 -> {
                if (expressionWrapper2.getType() == Expression.Type.ALIAS) {
                    yield from(expressionWrapper2.getWrappedExpression1().getType());
                } else {
                    yield GroupingType.NONE;
                }
            }
            default -> GroupingType.NONE;
        };
    }

    private static GroupingType from(Expression.Type type) {
        return switch (type) {
            case COUNT -> GroupingType.COUNT;
            case SUM -> GroupingType.SUM;
            case AVG -> GroupingType.AVG;
            case MIN -> GroupingType.MIN;
            case MAX -> GroupingType.MAX;
            default -> GroupingType.NONE;
        };
    }
}
