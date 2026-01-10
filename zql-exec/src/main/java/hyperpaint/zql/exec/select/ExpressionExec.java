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

            for (int i = 0; i  < list.size(); i++) {
                result[i] = new ExpressionExec(list.get(i));
            }

            return result;
        } else {
            return new ExpressionExec[]{new ExpressionExec(expression)};
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
            case COUNT, SUM, AVG, MIN, MAX -> buildExecEntry(expression.getWrappedExpression());
            default -> throw new IllegalStateException("Unexpected value: " + expression.getType());
        };
    }

    private static ExecEntry buildExecEntry(ExpressionWrapper2 expression) {
        return switch (expression.getType()) {
            case ALIAS -> buildExecEntry(expression.getWrappedExpression1());
            case JSON_PATH -> (path, data) -> {
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
            case COUNT, SUM, AVG, MIN, MAX -> buildExecRow(expression.getWrappedExpression());
            default -> throw new IllegalStateException("Unexpected value: " + expression.getType());
        };
    }

    private static ExecRow buildExecRow(ExpressionWrapper2 expression) {
        return switch (expression.getType()) {
            case ALIAS -> buildExecRow(expression.getWrappedExpression1());
            case JSON_PATH -> (row, columnsNameIndex) -> {
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
            default -> throw new IllegalStateException("Unexpected value: " + expression.getType());
        };
    }

    // endregion
}
