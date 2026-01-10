package hyperpaint.zql.exec.select;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.IntStream;

@Slf4j
class GroupingExec {
    private static class GroupingContainer {
        private final GroupingTypes[] types;
        private @Getter @Setter Object[] row;

        private final @Getter int[] counters;

        public GroupingContainer(GroupingTypes[] types) {
            this(types, null);
        }

        public GroupingContainer(GroupingTypes[] types, Object[] row) {
            this.types = types;
            this.row = row;

            Arrays.fill(counters = new int[this.row.length], 0);
        }

        public void add(GroupingContainer container) {
            for (int i = 0; i < row.length; i++) {
                switch (types[i]) {
                    case NONE -> {
                        // continue;
                    }
                    case COUNT -> {
                        if (row[i] instanceof Integer value) {
                            row[i] = value + 1;
                            counters[i]++;
                            continue;
                        } else if (row[i] instanceof Float value) {
                            row[i] = value.intValue() + 1;
                            counters[i]++;
                            continue;
                        }

                        log.warn("Grouping rule count contains non-number value '{}', skipped: '{}'", row[i], Arrays.toString(row));
                    }
                    case SUM -> {
                        if (row[i] instanceof Integer value1) {
                            if (container.row[i] instanceof Integer value2) {
                                row[i] = value1 + value2;
                                counters[i]++;
                                continue;
                            } else if (container.row[i] instanceof Float value2) {
                                row[i] = value1 + value2;
                                counters[i]++;
                                continue;
                            }
                        } else if (row[i] instanceof Float value1) {
                            if (container.row[i] instanceof Integer value2) {
                                row[i] = value1 + value2;
                                counters[i]++;
                                continue;
                            } else if (container.row[i] instanceof Float value2) {
                                row[i] = value1 + value2;
                                counters[i]++;
                                continue;
                            }
                        }

                        log.warn("Grouping rule sum contains non-number value '{}', skipped: '{}'", row[i], Arrays.toString(row));
                    }
                    case AVG -> {
                        if (row[i] instanceof Integer value1) {
                            if (container.row[i] instanceof Integer value2) {
                                row[i] = value1 + (value2 - value1) / (counters[i]++ + 1);
                                continue;
                            } else if (container.row[i] instanceof Float value2) {
                                row[i] = value1 + (value2 - value1) / (counters[i]++ + 1);
                                continue;
                            }
                        } else if (row[i] instanceof Float value1) {
                            if (container.row[i] instanceof Integer value2) {
                                row[i] = value1 + (value2 - value1) / (counters[i]++ + 1);
                                continue;
                            } else if (container.row[i] instanceof Float value2) {
                                row[i] = value1 + (value2 - value1) / (counters[i]++ + 1);
                                continue;
                            }
                        }

                        log.warn("Grouping rule avg contains non-number value '{}', skipped: '{}'", row[i], Arrays.toString(row));
                    }
                    case MIN -> {
                        if (row[i] instanceof Integer value1) {
                            if (container.row[i] instanceof Integer value2) {
                                row[i] = Math.min(value1, value2);
                                counters[i]++;
                                continue;
                            } else if (container.row[i] instanceof Float value2) {
                                row[i] = Math.min(value1, value2);
                                counters[i]++;
                                continue;
                            }
                        } else if (row[i] instanceof Float value1) {
                            if (container.row[i] instanceof Integer value2) {
                                row[i] = Math.min(value1, value2);
                                counters[i]++;
                                continue;
                            } else if (container.row[i] instanceof Float value2) {
                                row[i] = Math.min(value1, value2);
                                counters[i]++;
                                continue;
                            }
                        }

                        log.warn("Grouping rule min contains non-number value '{}', skipped: '{}'", row[i], Arrays.toString(row));
                    }
                    case MAX -> {
                        if (row[i] instanceof Integer value1) {
                            if (container.row[i] instanceof Integer value2) {
                                row[i] = Math.max(value1, value2);
                                counters[i]++;
                                continue;
                            } else if (container.row[i] instanceof Float value2) {
                                row[i] = Math.max(value1, value2);
                                counters[i]++;
                                continue;
                            }
                        } else if (row[i] instanceof Float value1) {
                            if (container.row[i] instanceof Integer value2) {
                                row[i] = Math.max(value1, value2);
                                counters[i]++;
                                continue;
                            } else if (container.row[i] instanceof Float value2) {
                                row[i] = Math.max(value1, value2);
                                counters[i]++;
                                continue;
                            }
                        }

                        log.warn("Grouping rule max contains non-number value '{}', skipped: '{}'", row[i], Arrays.toString(row));
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + types[i]);
                }
            }
        }

        @Override
        public int hashCode() {
            return IntStream.range(0, row.length).parallel().map(i -> types[i] == GroupingTypes.NONE ? row[i].hashCode() : 0).sum();
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof GroupingContainer another && IntStream.range(0, row.length).parallel().allMatch(i -> types[i] != GroupingTypes.NONE || Objects.equals(row[i], another.row[i]));
        }
    }

    private final GroupingTypes[] columnGroupingTypes;

    private final @Getter boolean skip;

    private GroupingExec(GroupingTypes[] columnGroupingTypes) {
        this.skip = Arrays.stream(columnGroupingTypes).allMatch(GroupingTypes::isNotGrouping);

        if (skip) {
            this.columnGroupingTypes = null;
        } else {
            this.columnGroupingTypes = columnGroupingTypes;
        }
    }

    public static GroupingExec get(GroupingTypes[] columnGroupingTypes) {
        return new GroupingExec(columnGroupingTypes);
    }

    public static void groupRows(GroupingExec exec, List<Object[]> rows) {
        if (exec.skip) {
            return;
        }

        final long startMilliseconds = System.currentTimeMillis();

        final Map<GroupingContainer, GroupingContainer> groups = new HashMap<>();
        final GroupingContainer current = new GroupingContainer(exec.columnGroupingTypes);

        rows.removeIf(row -> {
            current.setRow(row);

            GroupingContainer buff = groups.get(current);
            if (buff != null) {
                buff.add(current);
                return true;
            } else {
                buff = new GroupingContainer(exec.columnGroupingTypes, current.getRow());
                groups.put(buff, buff);
                return false;
            }
        });

        final long diffMilliseconds = System.currentTimeMillis() - startMilliseconds;
        if (log.isDebugEnabled()) {
            log.debug("Grouping rows took {} millis", diffMilliseconds);
        } else if (diffMilliseconds > 1000) {
            log.warn("Grouping rows took too long: {} millis", diffMilliseconds);
        }
    }
}
