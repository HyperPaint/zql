package hyperpaint.zql.exec.select;

import com.jayway.jsonpath.JsonPath;
import hyperpaint.zql.lang.expression.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class ExpressionExec {
    @FunctionalInterface
    private interface EntryValue {
        Object run(String path, String data);
    }

    @FunctionalInterface
    private interface RowValue {
        Object run(Object[] row, Map<String, Integer> columnsNameIndex);
    }

    private final EntryValue[] entryValue;
    private final RowValue[] rowValue;

    ExpressionExec(Expression expression) {
        entryValue = buildEntryValues(expression);
        rowValue = buildRowValues(expression);
    }

    private EntryValue[] buildEntryValues(Expression expression) {
        return switch (expression) {
            case ExpressionCollection expressionCollection -> buildEntryValues(expressionCollection);
            case ExpressionIdentifier expressionIdentifier -> new EntryValue[] { buildEntryValue(expressionIdentifier) };
            case ExpressionNumber expressionNumber -> new EntryValue[] { buildEntryValue(expressionNumber) };
            case ExpressionString expressionString -> new EntryValue[] { buildEntryValue(expressionString) };
            case ExpressionWrapper expressionWrapper -> new EntryValue[] { buildEntryValue(expressionWrapper) };
            case ExpressionWrapper2 expressionWrapper2 -> new EntryValue[] { buildEntryValue(expressionWrapper2) };
            default -> throw new IllegalStateException("Unexpected value: " + expression);
        };
    }

    private EntryValue[] buildEntryValues(ExpressionCollection expressionCollection) {
        final List<Expression> list = expressionCollection.getList();
        final EntryValue[] entryValues = new EntryValue[list.size()];

        for (int i = 0; i < list.size(); i++) {
            entryValues[i] = buildEntryValue(list.get(i));
        }

        return entryValues;
    }

    private EntryValue buildEntryValue(Expression expression) {
        return switch (expression) {
            case ExpressionIdentifier expressionIdentifier -> buildEntryValue(expressionIdentifier);
            case ExpressionNumber expressionNumber -> buildEntryValue(expressionNumber);
            case ExpressionString expressionString -> buildEntryValue(expressionString);
            case ExpressionWrapper expressionWrapper -> buildEntryValue(expressionWrapper);
            case ExpressionWrapper2 expressionWrapper2 -> buildEntryValue(expressionWrapper2);
            default -> throw new IllegalStateException("Unexpected value: " + expression);
        };
    }

    private EntryValue buildEntryValue(ExpressionIdentifier expression) {
        final String identifier = expression.getIdentifier().toLowerCase();

        return (path, data) -> {
            //noinspection CodeBlock2Expr
            return switch (identifier) {
                case "path" -> path;
                case "data" -> {
                    if (data == null) yield null;

                    try {
                        if (data.contains(".") || data.contains(",")) {
                            yield Float.parseFloat(data);
                        } else {
                            yield Integer.parseInt(data);
                        }
                    } catch (NumberFormatException ignored) {
                        yield data;
                    }
                }
                default -> throw new IllegalStateException("Unexpected value: " + expression.getIdentifier());
            };
        };
    }

    private EntryValue buildEntryValue(ExpressionNumber expression) {
        return (path, data) -> expression.getNumber();
    }

    private EntryValue buildEntryValue(ExpressionString expression) {
        return (path, data) -> expression.getString();
    }

    private EntryValue buildEntryValue(ExpressionWrapper expression) {
        return switch (expression.getType()) {
            case COUNT, SUM, AVG, MIN, MAX -> buildEntryValue(expression.getWrappedExpression());
            default -> throw new IllegalStateException("Unexpected value: " + expression.getType());
        };
    }

    private EntryValue buildEntryValue(ExpressionWrapper2 expression) {
        return switch (expression.getType()) {
            case ALIAS -> buildEntryValue(expression.getWrappedExpression1());
            case JSON_PATH -> (path, data) -> {
                final String json = String.valueOf(buildEntryValue(expression.getWrappedExpression1()).run(path, data));
                final String jsonPath = String.valueOf(buildEntryValue(expression.getWrappedExpression2()).run(path, data));

                try {
                    return JsonPath.read(json, jsonPath);
                } catch (Exception e) {
                    // todo log exception
                    e.printStackTrace();
                    return null;
                }
            };
            default -> throw new IllegalStateException("Unexpected value: " + expression.getType());
        };
    }

    private RowValue[] buildRowValues(Expression expression) {
        return switch (expression) {
            case ExpressionCollection expressionCollection -> buildRowValues(expressionCollection);
            case ExpressionIdentifier expressionIdentifier -> new RowValue[] { buildRowValue(expressionIdentifier) };
            case ExpressionNumber expressionNumber -> new RowValue[] { buildRowValue(expressionNumber) };
            case ExpressionString expressionString -> new RowValue[] { buildRowValue(expressionString) };
            case ExpressionWrapper expressionWrapper -> new RowValue[] { buildRowValue(expressionWrapper) };
            case ExpressionWrapper2 expressionWrapper2 -> new RowValue[] { buildRowValue(expressionWrapper2) };
            default -> throw new IllegalStateException("Unexpected value: " + expression);
        };
    }

    private RowValue[] buildRowValues(ExpressionCollection expressionCollection) {
        final List<Expression> list = expressionCollection.getList();
        final RowValue[] rowValues = new RowValue[list.size()];

        for (int i = 0; i < list.size(); i++) {
            rowValues[i] = buildRowValue(list.get(i));
        }

        return rowValues;
    }

    private RowValue buildRowValue(Expression expression) {
        return switch (expression) {
            case ExpressionIdentifier expressionIdentifier -> buildRowValue(expressionIdentifier);
            case ExpressionNumber expressionNumber -> buildRowValue(expressionNumber);
            case ExpressionString expressionString -> buildRowValue(expressionString);
            case ExpressionWrapper expressionWrapper -> buildRowValue(expressionWrapper);
            case ExpressionWrapper2 expressionWrapper2 -> buildRowValue(expressionWrapper2);
            default -> throw new IllegalStateException("Unexpected value: " + expression);
        };
    }

    private RowValue buildRowValue(ExpressionIdentifier expressionIdentifier) {
        return (row, columnsNameIndex) -> row[columnsNameIndex.get(expressionIdentifier.getIdentifier())];
    }

    private RowValue buildRowValue(ExpressionNumber expressionNumber) {
        return (row, columnsNameIndex) -> expressionNumber.getNumber();
    }

    private RowValue buildRowValue(ExpressionString expressionString) {
        return (row, columnsNameIndex) -> expressionString.getString();
    }

    private RowValue buildRowValue(ExpressionWrapper expressionWrapper) {
        return switch (expressionWrapper.getType()) {
            case COUNT, SUM, AVG, MIN, MAX -> buildRowValue(expressionWrapper.getWrappedExpression());
            default -> throw new IllegalStateException("Unexpected value: " + expressionWrapper.getType());
        };
    }

    private RowValue buildRowValue(ExpressionWrapper2 expressionWrapper2) {
        return switch (expressionWrapper2.getType()) {
            case ALIAS -> buildRowValue(expressionWrapper2.getWrappedExpression1());
            case JSON_PATH -> (row, columnsNameIndex) -> {
                final String json = String.valueOf(buildRowValue(expressionWrapper2.getWrappedExpression1()).run(row, columnsNameIndex));
                final String jsonPath = String.valueOf(buildRowValue(expressionWrapper2.getWrappedExpression2()).run(row, columnsNameIndex));

                try {
                    return JsonPath.read(json, jsonPath);
                } catch (Exception e) {
                    // todo log exception
                    e.printStackTrace();
                    return null;
                }
            };
            default -> throw new IllegalStateException("Unexpected value: " + expressionWrapper2.getType());
        };
    }

    public ArrayList<Object[]> entriesToRows(Map<String, String> entries) {
        if (entryValue == null) {
            return new ArrayList<>();
        }

        final var rows = new ArrayList<Object[]>(entries.size());

        for (var entry : entries.entrySet()) {
            final Object[] row = new Object[entryValue.length];

            for (int i = 0; i < entryValue.length; i++) {
                row[i] = entryValue[i].run(entry.getKey(), entry.getValue());
            }

            rows.add(row);
        }

        return rows;
    }

    @Deprecated
    public Object execute(String path, String data) {
        if (entryValue == null) {
            throw new IllegalStateException("Can't convert entry to value, select statement does not contain expressions");
        }

        return entryValue[0].run(path, data);
    }

    @Deprecated
    public Object execute(Object[] row, Map<String, Integer> columnsNameIndex) {
        if (rowValue == null) {
            throw new IllegalStateException("Can't convert row to value, select statement does not contain expressions");
        }

        return rowValue[0].run(row, columnsNameIndex);
    }
}
