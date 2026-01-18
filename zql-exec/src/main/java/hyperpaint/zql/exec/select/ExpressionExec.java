package hyperpaint.zql.exec.select;

import com.jayway.jsonpath.JsonPath;
import hyperpaint.zql.lang.expression.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
class ExpressionExec {
    @FunctionalInterface
    interface ExecEntry {
        Object run(String path, String data);
    }

    @FunctionalInterface
    interface ExecRow {
        Object run(Object[] row, Map<String, Integer> columnsNameIndex);
    }

    final ExecEntry execEntry;
    final ExecRow execRow;

    private ExpressionExec(Expression expression) {
        execEntry = buildExecEntry(expression);
        execRow = buildExecRow(expression);
    }

    public static ExpressionExec[] get(Expression expression) {
        if (expression == null) {
            return null;
        }

        if (expression instanceof ExpressionCollection expressionCollection) {
            final var list = expressionCollection.getList();
            final var result = new ExpressionExec[list.size()];

            for (int i = 0; i < list.size(); i++) {
                result[i] = new ExpressionExec(list.get(i));
            }

            return result;
        } else {
            return new ExpressionExec[] { new ExpressionExec(expression) };
        }
    }

    public static List<Object[]> convertEntriesToRows(ExpressionExec[] execs, Map<String, String> entries) {
        final long startMilliseconds = System.currentTimeMillis();

        final var rows = Collections.synchronizedList(new ArrayList<Object[]>(entries.size()));

        if (execs != null) {
            entries.entrySet().parallelStream().forEach(entry -> {
                final Object[] row = new Object[execs.length];

                for (int i = 0; i < execs.length; i++) {
                    row[i] = execs[i].execEntry.run(entry.getKey(), entry.getValue());
                }

                rows.add(row);
            });
        }

        final long diffMilliseconds = System.currentTimeMillis() - startMilliseconds;
        if (log.isDebugEnabled()) {
            log.debug("Converting entries to rows took {} millis", diffMilliseconds);
        } else if (diffMilliseconds > 1000) {
            log.warn("Converting entries to rows took too long: {} millis", diffMilliseconds);
        }

        return rows;
    }

    // region ExecEntry

    static ExecEntry buildExecEntry(Expression expression) {
        return switch (expression) {
            case ExpressionIdentifier expressionIdentifier -> buildExecEntry(expressionIdentifier);
            case ExpressionNumber expressionNumber -> buildExecEntry(expressionNumber);
            case ExpressionString expressionString -> buildExecEntry(expressionString);
            case ExpressionWrapper expressionWrapper -> buildExecEntry(expressionWrapper);
            case ExpressionWrapper2 expressionWrapper2 -> buildExecEntry(expressionWrapper2);
            case ExpressionWrapper3 expressionWrapper3 -> buildExecEntry(expressionWrapper3);
            default -> throw new IllegalStateException("Unexpected value: " + expression);
        };
    }

    private static ExecEntry buildExecEntry(ExpressionIdentifier expression) {
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

    private static ExecEntry buildExecEntry(ExpressionNumber expression) {
        return (path, data) -> expression.getNumber();
    }

    private static ExecEntry buildExecEntry(ExpressionString expression) {
        return (path, data) -> expression.getString();
    }

    private static ExecEntry buildExecEntry(ExpressionWrapper expression) {
        return switch (expression.getType()) {
            case G1A_COUNT, G1A_SUM, G1A_AVG, G1A_MIN, G1A_MAX, E1A_ARITHMETICAL_WRAP -> buildExecEntry(expression.getWrappedExpression());
            default -> throw new IllegalStateException("Unexpected value: " + expression.getType());
        };
    }

    private static ExecEntry buildExecEntry(ExpressionWrapper2 expression) {
        return switch (expression.getType()) {
            case ALIAS -> buildExecEntry(expression.getWrappedExpression1());
            case E2A_JSON_PATH -> (path, data) -> {
                final String json = String.valueOf(buildExecEntry(expression.getWrappedExpression1()).run(path, data));
                final String jsonPath = String.valueOf(buildExecEntry(expression.getWrappedExpression2()).run(path, data));

                try {
                    return JsonPath.read(json, jsonPath);
                } catch (Exception e) {
                    log.warn("Can't execute entry expression 'json_path(\"{}\", \"{}\")', skipped", json, jsonPath);
                    log.warn(e.getMessage(), e);
                    return null;
                }
            };
            case E2A_CONCATENATION -> (path, data) -> {
                final String left = String.valueOf(buildExecEntry(expression.getWrappedExpression1()).run(path, data));
                final String right = String.valueOf(buildExecEntry(expression.getWrappedExpression2()).run(path, data));
                return left + right;
            };
            case E2A_ARITHMETICAL_PLUS -> (path, data) -> {
                final float left;
                final Object leftObject = buildExecEntry(expression.getWrappedExpression1()).run(path, data);

                if (leftObject instanceof Number number) {
                    left = number.floatValue();
                } else {
                    try {
                        left = Float.parseFloat(leftObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert left part of summary to number, it is not number: {}, skipping...", leftObject);
                        return Float.NaN;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert left part of summary to number, it is null: {}, skipping...", leftObject);
                        return Float.NaN;
                    }
                }

                final float right;
                final Object rightObject = buildExecEntry(expression.getWrappedExpression2()).run(path, data);

                if (rightObject instanceof Number number) {
                    right = number.floatValue();
                } else {
                    try {
                        right = Float.parseFloat(rightObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert right part of summary to number, it is not number: {}, skipping...", rightObject);
                        return Float.NaN;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert right part of summary to number, it is null: {}, skipping...", rightObject);
                        return Float.NaN;
                    }
                }

                return left + right;
            };
            case E2A_ARITHMETICAL_MINUS -> (path, data) -> {
                final float left;
                final Object leftObject = buildExecEntry(expression.getWrappedExpression1()).run(path, data);

                if (leftObject instanceof Number number) {
                    left = number.floatValue();
                } else {
                    try {
                        left = Float.parseFloat(leftObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert left part of subtraction to number, it is not number: {}, skipping...", leftObject);
                        return Float.NaN;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert left part of subtraction to number, it is null: {}, skipping...", leftObject);
                        return Float.NaN;
                    }
                }

                final float right;
                final Object rightObject = buildExecEntry(expression.getWrappedExpression2()).run(path, data);

                if (rightObject instanceof Number number) {
                    right = number.floatValue();
                } else {
                    try {
                        right = Float.parseFloat(rightObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert right part of subtraction to number, it is not number: {}, skipping...", rightObject);
                        return Float.NaN;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert right part of subtraction to number, it is null: {}, skipping...", rightObject);
                        return Float.NaN;
                    }
                }

                return left - right;
            };
            case E2A_ARITHMETICAL_MULTIPLY -> (path, data) -> {
                final float left;
                final Object leftObject = buildExecEntry(expression.getWrappedExpression1()).run(path, data);

                if (leftObject instanceof Number number) {
                    left = number.floatValue();
                } else {
                    try {
                        left = Float.parseFloat(leftObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert left part of multiply to number, it is not number: {}, skipping...", leftObject);
                        return Float.NaN;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert left part of multiply to number, it is null: {}, skipping...", leftObject);
                        return Float.NaN;
                    }
                }

                final float right;
                final Object rightObject = buildExecEntry(expression.getWrappedExpression2()).run(path, data);

                if (rightObject instanceof Number number) {
                    right = number.floatValue();
                } else {
                    try {
                        right = Float.parseFloat(rightObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert right part of multiply to number, it is not number: {}, skipping...", rightObject);
                        return Float.NaN;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert right part of multiply to number, it is null: {}, skipping...", rightObject);
                        return Float.NaN;
                    }
                }

                return left * right;
            };
            case E2A_ARITHMETICAL_DIVIDE -> (path, data) -> {
                final float left;
                final Object leftObject = buildExecEntry(expression.getWrappedExpression1()).run(path, data);

                if (leftObject instanceof Number number) {
                    left = number.floatValue();
                } else {
                    try {
                        left = Float.parseFloat(leftObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert left part of div to number, it is not number: {}, skipping...", leftObject);
                        return Float.NaN;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert left part of div to number, it is null: {}, skipping...", leftObject);
                        return Float.NaN;
                    }
                }

                final float right;
                final Object rightObject = buildExecEntry(expression.getWrappedExpression2()).run(path, data);

                if (rightObject instanceof Number number) {
                    right = number.floatValue();
                } else {
                    try {
                        right = Float.parseFloat(rightObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert right part of div to number, it is not number: {}, skipping...", rightObject);
                        return Float.NaN;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert right part of div to number, it is null: {}, skipping...", rightObject);
                        return Float.NaN;
                    }
                }

                if (right == 0f) {
                    return Float.NaN;
                } else {
                    return left / right;
                }
            };
            default -> throw new IllegalStateException("Unexpected value: " + expression.getType());
        };
    }

    private static ExecEntry buildExecEntry(ExpressionWrapper3 expression) {
        return switch (expression.getType()) {
            case E3A_SUBSTRING -> (path, data) -> {
                final String first = String.valueOf(buildExecEntry(expression.getWrappedExpression1()).run(path, data));

                final int from;
                final Object fromObject = buildExecEntry(expression.getWrappedExpression2()).run(path, data);

                if (fromObject instanceof Number number) {
                    from = number.intValue();
                } else {
                    try {
                        from = Integer.parseInt(fromObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert from part of substring to integer, it is not number: {}, skipping...", fromObject);
                        return null;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert from part of substring to integer, it is null: {}, skipping...", fromObject);
                        return null;
                    }
                }

                final int to;
                final Object toObject = buildExecEntry(expression.getWrappedExpression3()).run(path, data);

                if (toObject instanceof Number number) {
                    to = number.intValue();
                } else {
                    try {
                        to = Integer.parseInt(toObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert to part of substring to integer, it is not number: {}, skipping...", toObject);
                        return null;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert to part of substring to integer, it is null: {}, skipping...", toObject);
                        return null;
                    }
                }

                return first.substring(Math.min(first.length(), from), Math.min(first.length(), to));
            };
            default -> throw new IllegalStateException("Unexpected value: " + expression.getType());
        };
    }

    // endregion

    // region ExecRow

    static ExecRow buildExecRow(Expression expression) {
        return switch (expression) {
            case ExpressionIdentifier expressionIdentifier -> buildExecRow(expressionIdentifier);
            case ExpressionNumber expressionNumber -> buildExecRow(expressionNumber);
            case ExpressionString expressionString -> buildExecRow(expressionString);
            case ExpressionWrapper expressionWrapper -> buildExecRow(expressionWrapper);
            case ExpressionWrapper2 expressionWrapper2 -> buildExecRow(expressionWrapper2);
            case ExpressionWrapper3 expressionWrapper3 -> buildExecRow(expressionWrapper3);
            default -> throw new IllegalStateException("Unexpected value: " + expression);
        };
    }

    private static ExecRow buildExecRow(ExpressionIdentifier expression) {
        return (row, columnsNameIndex) -> row[columnsNameIndex.get(expression.getIdentifier())];
    }

    private static ExecRow buildExecRow(ExpressionNumber expression) {
        return (row, columnsNameIndex) -> expression.getNumber();
    }

    private static ExecRow buildExecRow(ExpressionString expression) {
        return (row, columnsNameIndex) -> expression.getString();
    }

    private static ExecRow buildExecRow(ExpressionWrapper expression) {
        return switch (expression.getType()) {
            case G1A_COUNT, G1A_SUM, G1A_AVG, G1A_MIN, G1A_MAX, E1A_ARITHMETICAL_WRAP -> buildExecRow(expression.getWrappedExpression());
            default -> throw new IllegalStateException("Unexpected value: " + expression.getType());
        };
    }

    private static ExecRow buildExecRow(ExpressionWrapper2 expression) {
        return switch (expression.getType()) {
            case ALIAS -> buildExecRow(expression.getWrappedExpression1());
            case E2A_JSON_PATH -> (row, columnsNameIndex) -> {
                final String json = String.valueOf(buildExecRow(expression.getWrappedExpression1()).run(row, columnsNameIndex));
                final String jsonPath = String.valueOf(buildExecRow(expression.getWrappedExpression2()).run(row, columnsNameIndex));

                try {
                    return JsonPath.read(json, jsonPath);
                } catch (Exception e) {
                    log.warn("Can't execute row expression 'json_path(\"{}\", \"{}\")', skipped", json, jsonPath);
                    log.warn(e.getMessage(), e);
                    return null;
                }
            };
            case E2A_CONCATENATION -> (row, columnsNameIndex) -> {
                final String left = String.valueOf(buildExecRow(expression.getWrappedExpression1()).run(row, columnsNameIndex));
                final String right = String.valueOf(buildExecRow(expression.getWrappedExpression2()).run(row, columnsNameIndex));
                return left + right;
            };
            case E2A_ARITHMETICAL_PLUS -> (row, columnsNameIndex) -> {
                final float left;
                final Object leftObject = buildExecRow(expression.getWrappedExpression1()).run(row, columnsNameIndex);

                if (leftObject instanceof Number number) {
                    left = number.floatValue();
                } else {
                    try {
                        left = Float.parseFloat(leftObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert left part of summary to number, it is not number: {}, skipping...", leftObject);
                        return Float.NaN;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert left part of summary to number, it is null: {}, skipping...", leftObject);
                        return Float.NaN;
                    }
                }

                final float right;
                final Object rightObject = buildExecRow(expression.getWrappedExpression2()).run(row, columnsNameIndex);

                if (rightObject instanceof Number number) {
                    right = number.floatValue();
                } else {
                    try {
                        right = Float.parseFloat(rightObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert right part of summary to number, it is not number: {}, skipping...", rightObject);
                        return Float.NaN;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert right part of summary to number, it is null: {}, skipping...", rightObject);
                        return Float.NaN;
                    }
                }

                return left + right;
            };
            case E2A_ARITHMETICAL_MINUS -> (row, columnsNameIndex) -> {
                final float left;
                final Object leftObject = buildExecRow(expression.getWrappedExpression1()).run(row, columnsNameIndex);

                if (leftObject instanceof Number number) {
                    left = number.floatValue();
                } else {
                    try {
                        left = Float.parseFloat(leftObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert left part of subtraction to number, it is not number: {}, skipping...", leftObject);
                        return Float.NaN;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert left part of subtraction to number, it is null: {}, skipping...", leftObject);
                        return Float.NaN;
                    }
                }

                final float right;
                final Object rightObject = buildExecRow(expression.getWrappedExpression2()).run(row, columnsNameIndex);

                if (rightObject instanceof Number number) {
                    right = number.floatValue();
                } else {
                    try {
                        right = Float.parseFloat(rightObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert right part of subtraction to number, it is not number: {}, skipping...", rightObject);
                        return Float.NaN;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert right part of subtraction to number, it is null: {}, skipping...", rightObject);
                        return Float.NaN;
                    }
                }

                return left - right;
            };
            case E2A_ARITHMETICAL_MULTIPLY -> (row, columnsNameIndex) -> {
                final float left;
                final Object leftObject = buildExecRow(expression.getWrappedExpression1()).run(row, columnsNameIndex);

                if (leftObject instanceof Number number) {
                    left = number.floatValue();
                } else {
                    try {
                        left = Float.parseFloat(leftObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert left part of multiply to number, it is not number: {}, skipping...", leftObject);
                        return Float.NaN;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert left part of multiply to number, it is null: {}, skipping...", leftObject);
                        return Float.NaN;
                    }
                }

                final float right;
                final Object rightObject = buildExecRow(expression.getWrappedExpression2()).run(row, columnsNameIndex);

                if (rightObject instanceof Number number) {
                    right = number.floatValue();
                } else {
                    try {
                        right = Float.parseFloat(rightObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert right part of multiply to number, it is not number: {}, skipping...", rightObject);
                        return Float.NaN;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert right part of multiply to number, it is null: {}, skipping...", rightObject);
                        return Float.NaN;
                    }
                }

                return left * right;
            };
            case E2A_ARITHMETICAL_DIVIDE -> (row, columnsNameIndex) -> {
                final float left;
                final Object leftObject = buildExecRow(expression.getWrappedExpression1()).run(row, columnsNameIndex);

                if (leftObject instanceof Number number) {
                    left = number.floatValue();
                } else {
                    try {
                        left = Float.parseFloat(leftObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert left part of div to number, it is not number: {}, skipping...", leftObject);
                        return Float.NaN;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert left part of div to number, it is null: {}, skipping...", leftObject);
                        return Float.NaN;
                    }
                }

                final float right;
                final Object rightObject = buildExecRow(expression.getWrappedExpression2()).run(row, columnsNameIndex);

                if (rightObject instanceof Number number) {
                    right = number.floatValue();
                } else {
                    try {
                        right = Float.parseFloat(rightObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert right part of div to number, it is not number: {}, skipping...", rightObject);
                        return Float.NaN;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert right part of div to number, it is null: {}, skipping...", rightObject);
                        return Float.NaN;
                    }
                }

                if (right == 0f) {
                    return Float.NaN;
                } else {
                    return left / right;
                }
            };
            default -> throw new IllegalStateException("Unexpected value: " + expression.getType());
        };
    }

    private static ExecRow buildExecRow(ExpressionWrapper3 expression) {
        return switch (expression.getType()) {
            case E3A_SUBSTRING -> (row, columnsNameIndex) -> {
                final String first = String.valueOf(buildExecRow(expression.getWrappedExpression1()).run(row, columnsNameIndex));

                final int from;
                final Object fromObject = buildExecRow(expression.getWrappedExpression2()).run(row, columnsNameIndex);

                if (fromObject instanceof Number number) {
                    from = number.intValue();
                } else {
                    try {
                        from = Integer.parseInt(fromObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert from part of substring to integer, it is not number: {}, skipping...", fromObject);
                        return null;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert from part of substring to integer, it is null: {}, skipping...", fromObject);
                        return null;
                    }
                }

                final int to;
                final Object toObject = buildExecRow(expression.getWrappedExpression3()).run(row, columnsNameIndex);

                if (toObject instanceof Number number) {
                    to = number.intValue();
                } else {
                    try {
                        to = Integer.parseInt(toObject.toString());
                    } catch (NumberFormatException e) {
                        log.warn("Can't convert to part of substring to integer, it is not number: {}, skipping...", toObject);
                        return null;
                    } catch (NullPointerException e) {
                        log.warn("Can't convert to part of substring to integer, it is null: {}, skipping...", toObject);
                        return null;
                    }
                }

                return first.substring(Math.min(first.length() - 1, from), Math.min(first.length() - 1, to));
            };
            default -> throw new IllegalStateException("Unexpected value: " + expression.getType());
        };
    }

    // endregion
}
