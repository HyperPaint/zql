package hyperpaint.zql.exec.select;

import hyperpaint.zql.lang.expression.Expression;

import java.util.Map;

class AnalyzerExec {
    /** Наименования полей */
    private String[] columnsName;

    /** Индексы наименований полей */
    private Map<String, Integer> columnsNameIndex;

    /** Тип выражений в полях */
    private Expression.Type[] columnsExpressionType;

    /** Индексы группировки полей */
    private int[] groupingIndex;

    AnalyzerExec(SelectStatement selectStatement) {

    }
}
