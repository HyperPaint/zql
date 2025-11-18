package hyperpaint.zql.exec.select;

import com.jayway.jsonpath.JsonPath;
import hyperpaint.zql.lang.expression.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class ExpressionExec {
    @FunctionalInterface
    private interface EntryToValue {
        Object run(String path, String data);
    }

    @FunctionalInterface
    private interface RowToValue {
        Object run(Object[] row, Map<String, Integer> columnsNameIndex);
    }

    private final EntryToValue[] entryToValue;
    private final RowToValue[] rowToValue;

    ExpressionExec(Expression expression) {
        switch (expression) {
            case ExpressionCollection expressionCollection -> {
                final List<Expression> list = expressionCollection.getList();

                entryToValue = new EntryToValue[list.size()];
                rowToValue = new RowToValue[list.size()];

                for (int i = 0; i < list.size(); i++) {
                    final ExpressionExec expression1 = new ExpressionExec(list.get(i));

                    entryToValue[i] = expression1.entryToValue[0];
                    rowToValue[i] = expression1.rowToValue[0];
                }
            }
            case ExpressionIdentifier expressionIdentifier -> {
                entryToValue = new EntryToValue[]{(path, data) -> switch (expressionIdentifier.getIdentifier().toLowerCase()) {
                    case "path" -> path;
                    case "data" -> {
                        if (data == null) {
                            yield null;
                        }

                        try {
                            if (data.contains(".") || data.contains(",")) {
                                yield Float.valueOf(data);
                            } else {
                                yield Integer.valueOf(data);
                            }
                        } catch (NumberFormatException ignored) {
                            yield data;
                        }
                    }
                    case null, default -> throw new IllegalArgumentException("Unexpected value: " + expressionIdentifier.getIdentifier());
                }};
                rowToValue = new RowToValue[]{(row, columnsNameIndex) -> row[columnsNameIndex.get(expressionIdentifier.getIdentifier())]};
            }
            case ExpressionNumber expressionNumber -> {
                entryToValue = new EntryToValue[]{(path, data) -> expressionNumber.getNumber()};
                rowToValue = new RowToValue[]{(row, columnsNameIndex) -> expressionNumber.getNumber()};
            }
            case ExpressionString expressionString -> {
                entryToValue = new EntryToValue[]{(path, data) -> expressionString.getString()};
                rowToValue = new RowToValue[]{(row, columnsNameIndex) -> expressionString.getString()};
            }
            case ExpressionWrapper expressionWrapper -> {
                final ExpressionExec expression1 = new ExpressionExec(expressionWrapper.getWrappedExpression());

                switch (expressionWrapper.getType()) {
                    case COUNT, SUM, AVG, MIN, MAX -> {
                        entryToValue = expression1.entryToValue;
                        rowToValue = expression1.rowToValue;
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + expressionWrapper.getType());
                }
            }
            case ExpressionWrapper2 expressionWrapper2 -> {
                final ExpressionExec expression1 = new ExpressionExec(expressionWrapper2.getWrappedExpression1());
                final ExpressionExec expression2 = new ExpressionExec(expressionWrapper2.getWrappedExpression2());

                switch (expressionWrapper2.getType()) {
                    case ALIAS -> {
                        entryToValue = new ExpressionExec(expressionWrapper2.getWrappedExpression1()).entryToValue;
                        rowToValue = new ExpressionExec(expressionWrapper2.getWrappedExpression1()).rowToValue;
                    }
                    case JSON_PATH -> {
                        entryToValue = new EntryToValue[]{(path, data) -> {
                            final String json = String.valueOf(expression1.entryToValue[0].run(path, data));
                            final String jsonPath = String.valueOf(expression2.entryToValue[0].run(path, data));

                            try {
                                return JsonPath.read(json, jsonPath);
                            } catch (Exception ignored) {
                                return null;
                            }
                        }};
                        rowToValue = new RowToValue[]{(row, columnsNameIndex) -> {
                            final String json = String.valueOf(expression1.rowToValue[0].run(row, columnsNameIndex));
                            final String jsonPath = String.valueOf(expression2.rowToValue[0].run(row, columnsNameIndex));

                            try {
                                return JsonPath.read(json, jsonPath);
                            } catch (Exception ignored) {
                                return null;
                            }
                        }};
                    }
                    default -> throw new IllegalArgumentException("Unexpected value: " + expressionWrapper2.getType());
                }
            }
            case null -> {
                entryToValue = null;
                rowToValue = null;
            }
            default -> throw new IllegalArgumentException("Unexpected value: " + expression);
        }
    }

    public ArrayList<Object[]> entriesToRows(Map<String, String> entries) {
        if (entryToValue == null) {
            return new ArrayList<>();
        }

        final var rows = new ArrayList<Object[]>(entries.size());

        for (var entry : entries.entrySet()) {
            final Object[] row = new Object[entryToValue.length];

            for (int i = 0; i < entryToValue.length; i++) {
                row[i] = entryToValue[i].run(entry.getKey(), entry.getValue());
            }

            rows.add(row);
        }

        return rows;
    }

    public Object execute(String path, String data) {
        if (entryToValue == null) {
            throw new IllegalStateException("Can't convert entry to value, select statement does not contain expressions");
        }

        return entryToValue[0].run(path, data);
    }

    public Object execute(Object[] row, Map<String, Integer> columnsNameIndex) {
        if (rowToValue == null) {
            throw new IllegalStateException("Can't convert row to value, select statement does not contain expressions");
        }

        return rowToValue[0].run(row, columnsNameIndex);
    }
}
