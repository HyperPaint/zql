package hyperpaint.zql.exec.select;

import hyperpaint.zql.exec.ResultSet;
import hyperpaint.zql.exec.Statement;
import hyperpaint.zql.lang.ZQLException;
import hyperpaint.zql.lang.expression.*;
import hyperpaint.zql.lang.statement.Select;
import lombok.NonNull;
import org.apache.zookeeper.ZooKeeper;

import java.util.*;

public class SelectStatement implements Statement {
    private final ExpressionExec expressionExec;
    private final ZnodeExec znodeExec;
    private final FilteringExec whereFilteringExec;
    private final GroupingExec groupingExec;
    private final FilteringExec havingFilteringExec;
    private final SortingExec sortingExec;

    /** Наименования полей */
    private String[] columnNames;
    /** Индексы наименований полей */
    private Map<String, Integer> columnNameIndexes;

    /** Тип группировки в полях */
    private GroupingType[] columnGroupingTypes;

    /** Тип сортировки в полях */
    private SortingType[] columnSortingTypes;

    /** Ошибки возникшие во время исполнения */
    private final List<Exception> exceptions = new LinkedList<>();

    public SelectStatement(@NonNull Select select) throws ZQLException {
        analyze(select);

        expressionExec = new ExpressionExec(select.getSelectExpression());
        znodeExec = new ZnodeExec(select.getFromZnode());
        whereFilteringExec = new FilteringExec(select.getWhereCondition());
        groupingExec = new GroupingExec(columnGroupingTypes, exceptions);
        havingFilteringExec = new FilteringExec(select.getHavingCondition());
        sortingExec = new SortingExec(columnSortingTypes);
    }

    @Override
    public ResultSet execute(ZooKeeper zookeeper) throws ZQLException {
        try {
            exceptions.clear();

            final var entries = znodeExec.znodesToEntries(zookeeper);
            whereFilteringExec.execute(entries);
            final var rows = expressionExec.entriesToRows(entries);
            groupingExec.execute(rows);
            havingFilteringExec.execute(rows, columnNameIndexes);
            sortingExec.execute(rows);

            return new ResultSet(columnNames, columnNameIndexes, rows);
        } catch (Exception e) {
            throw new ZQLException(e);
        }
    }

    private void analyze(Select select) throws ZQLException {
        // Проверка при отсутствии select
        if (!select.hasSelectExpression() && select.hasFromZnode()) {
            throw new ZQLException("Select statement not contains select expressions but contains from znodes");
        }

        if (!select.hasSelectExpression() && select.hasWhereCondition()) {
            throw new ZQLException("Select statement not contains select expressions but contains where conditions");
        }

        if (!select.hasSelectExpression() && select.hasGroupByExpression()) {
            throw new ZQLException("Select statement not contains select expressions but contains group by expressions");
        }

        if (!select.hasSelectExpression() && select.hasOrderByExpression()) {
            throw new ZQLException("Select statement not contains select expressions but contains order by expressions");
        }

        if (!select.hasSelectExpression() && select.hasHavingCondition()) {
            throw new ZQLException("Select statement not contains select expressions but contains having conditions");
        }

        // Проверка при отсутствии znode
        if (!select.hasFromZnode() && select.hasWhereCondition()) {
            throw new ZQLException("Select statement not contains from znode but contains where conditions");
        }

        if (!select.hasFromZnode() && select.hasGroupByExpression()) {
            throw new ZQLException("Select statement not contains from znode but contains group by expressions");
        }

        if (!select.hasFromZnode() && select.hasHavingCondition()) {
            throw new ZQLException("Select statement not contains from znode but contains having conditions");
        }

        if (!select.hasFromZnode() && select.hasOrderByExpression()) {
            throw new ZQLException("Select statement not contains from znode but contains order by expressions");
        }

        // Проверка при отсутствии группировки
        if (!select.hasGroupByExpression() && select.hasHavingCondition()) {
            throw new ZQLException("Select statement not contains group by expressions but contains having conditions");
        }

        // Наименования полей и их индексы
        final List<Expression> selectExpressions;

        if (select.getSelectExpression() == null) {
            selectExpressions = new ArrayList<>(0);

            columnNames = new String[0];
            columnNameIndexes = new HashMap<>(0);

            columnGroupingTypes = new GroupingType[0];
            columnSortingTypes = new SortingType[0];
        } else {
            if (select.getSelectExpression() instanceof ExpressionCollection expressionCollection) {
                selectExpressions = expressionCollection.getList();
            } else {
                selectExpressions = new ArrayList<>(List.of(select.getSelectExpression()));
            }

            columnNames = new String[selectExpressions.size()];
            columnNameIndexes = new HashMap<>(selectExpressions.size());

            collectColumnNames(selectExpressions, columnNames, columnNameIndexes);

            columnGroupingTypes = new GroupingType[selectExpressions.size()];
            columnSortingTypes = new SortingType[selectExpressions.size()];
        }

        // Наименования полей группировки и их индексы
        final String[] columnGroupingNames;
        final Map<String, Integer> columnGroupingNameIndexes;

        final List<Expression> groupingExpressions;

        if (select.getGroupByExpression() == null) {
            groupingExpressions = new ArrayList<>(0);

            columnGroupingNames = new String[0];
            columnGroupingNameIndexes = new HashMap<>(0);
        } else {
            if (select.getGroupByExpression() instanceof ExpressionCollection expressionCollection) {
                groupingExpressions = expressionCollection.getList();
            } else {
                groupingExpressions = new ArrayList<>(List.of(select.getGroupByExpression()));
            }

            columnGroupingNames = new String[groupingExpressions.size()];
            columnGroupingNameIndexes = new HashMap<>(groupingExpressions.size());

            collectColumnNames(groupingExpressions, columnGroupingNames, columnGroupingNameIndexes);
        }

        // Инициализация типов группировки полей и проверка группировки полей
        for (int i = 0; i < selectExpressions.size(); i++) {
            columnGroupingTypes[i] = GroupingType.from(selectExpressions.get(i));

            if (columnGroupingTypes[i] != GroupingType.NONE) {
                continue;
            }

            final int index = columnGroupingNameIndexes.getOrDefault(columnNames[i], -1);

            if (index == -1) {
                throw new ZQLException("Grouping rule not contains expression: " + columnNames[i]);
            }
        }

        for (int i = 0; i < groupingExpressions.size(); i++) {
            final int index = columnNameIndexes.getOrDefault(columnGroupingNames[i], -1);

            if (index == -1) {
                throw new ZQLException("Grouping rule contains non-exists expression: " + columnGroupingNames[i]);
            }
        }

        // Наименования полей сортировки и их индексы
        final String[] columnSortingNames;
        final Map<String, Integer> columnSortingNameIndexes;

        final List<Expression> sortingExpressions;

        if (select.getOrderByExpression() == null) {
            sortingExpressions = new ArrayList<>(0);

            columnSortingNames = new String[0];
            columnSortingNameIndexes = new HashMap<>(0);
        } else {
            if (select.getOrderByExpression() instanceof ExpressionCollection expressionCollection) {
                sortingExpressions = expressionCollection.getList();
            } else {
                sortingExpressions = new ArrayList<>(List.of(select.getOrderByExpression()));
            }

            columnSortingNames = new String[sortingExpressions.size()];
            columnSortingNameIndexes = new HashMap<>(sortingExpressions.size());

            collectColumnNames(sortingExpressions, columnSortingNames, columnSortingNameIndexes);
        }

        // Инициализация типов сортировки полей и проверка сортировки полей
        Arrays.fill(columnSortingTypes, SortingType.NONE);

        for (int i = 0; i < sortingExpressions.size(); i++) {
            final int index = columnNameIndexes.getOrDefault(columnSortingNames[i], -1);

            if (index == -1) {
                throw new ZQLException("Sorting rule contains non-exists expression: " + sortingExpressions.get(i).toZql());
            }

            columnSortingTypes[index] = SortingType.from(sortingExpressions.get(i));
        }
    }

    private void collectColumnNames(List<Expression> expressions, String[] columnNames, Map<String, Integer> columnNameIndexes) {
        for (int i = 0; i < expressions.size(); i++) {
            switch (expressions.get(i)) {
                case ExpressionIdentifier expressionIdentifier -> {
                    columnNames[i] = expressionIdentifier.getIdentifier();
                    columnNameIndexes.put(columnNames[i], i);
                }
                case ExpressionNumber expressionNumber -> {
                    columnNames[i] = String.valueOf(expressionNumber.getNumber());
                    columnNameIndexes.put(columnNames[i], i);
                }
                case ExpressionString expressionString -> {
                    columnNames[i] = expressionString.getString();
                    columnNameIndexes.put(columnNames[i], i);
                }
                case ExpressionWrapper expressionWrapper -> {
                    switch (expressionWrapper.getType()) {
                        case COUNT, SUM, AVG, MIN, MAX -> {
                            columnNames[i] = expressionWrapper.toZql();
                            columnNameIndexes.put(columnNames[i], i);
                        }
                        case ORDER_BY_ASC, ORDER_BY_DESC -> {
                            columnNames[i] = expressionWrapper.getWrappedExpression().toZql();
                            columnNameIndexes.put(columnNames[i], i);
                        }
                        default -> throw new IllegalStateException("Unexpected value: " + expressionWrapper.getType());
                    }
                }
                case ExpressionWrapper2 expressionWrapper2 -> {
                    switch (expressionWrapper2.getType()) {
                        case ALIAS -> {
                            columnNames[i] = expressionWrapper2.getWrappedExpression2().toZql();
                            columnNameIndexes.put(columnNames[i], i);
                            columnNameIndexes.put(expressionWrapper2.getWrappedExpression1().toZql(), i);
                        }
                        case JSON_PATH -> {
                            columnNames[i] = expressionWrapper2.toZql();
                            columnNameIndexes.put(columnNames[i], i);
                        }
                        default -> throw new IllegalStateException("Unexpected value: " + expressionWrapper2.getType());
                    }
                }
                default -> throw new IllegalArgumentException("Unexpected value: " + expressions.get(i));
            }
        }
    }
}
