package hyperpaint.zql.exec.select;

import hyperpaint.zql.lang.ZQLException;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.IntStream;

class GroupingWrapper {
    private @Getter @Setter Object[] row;

    private final GroupingType[] types;
    private final List<Exception> exceptions;

    private int avgCounter = 0;

    GroupingWrapper(GroupingType[] types, List<Exception> exceptions) {
        this(null, types, exceptions);
    }

    GroupingWrapper(Object[] row, GroupingType[] types, List<Exception> exceptions) {
        this.row = row;
        this.types = types;
        this.exceptions = exceptions;
    }

    void add(GroupingWrapper another) {
        for (int i = 0; i < row.length; i++) {
            switch (types[i]) {
                case NONE -> {
                    // continue;
                }
                case COUNT -> {
                    if (row[i] instanceof Integer value) {
                        row[i] = value + 1;
                        continue;
                    } else if (row[i] instanceof Float value) {
                        row[i] = value.intValue() + 1;
                        continue;
                    }

                    exceptions.add(new ZQLException("Grouping rule COUNT contains non-number value, skipped: " + Arrays.toString(row)));
                }
                case SUM -> {
                    if (row[i] instanceof Integer value1) {
                        if (another.row[i] instanceof Integer value2) {
                            row[i] = value1 + value2;
                            continue;
                        } else if (another.row[i] instanceof Float value2) {
                            row[i] = value1 + value2;
                            continue;
                        }
                    } else if (row[i] instanceof Float value1) {
                        if (another.row[i] instanceof Integer value2) {
                            row[i] = value1 + value2;
                            continue;
                        } else if (another.row[i] instanceof Float value2) {
                            row[i] = value1 + value2;
                            continue;
                        }
                    }

                    exceptions.add(new ZQLException("Grouping rule SUM contains non-number value, skipped: " + Arrays.toString(row)));
                }
                case AVG -> {
                    if (row[i] instanceof Integer value1) {
                        if (another.row[i] instanceof Integer value2) {
                            row[i] = value1 + (value2 - value1) / (avgCounter++ + 1);
                            continue;
                        } else if (another.row[i] instanceof Float value2) {
                            row[i] = value1 + (value2 - value1) / (avgCounter++ + 1);
                            continue;
                        }
                    } else if (row[i] instanceof Float value1) {
                        if (another.row[i] instanceof Integer value2) {
                            row[i] = value1 + (value2 - value1) / (avgCounter++ + 1);
                            continue;
                        } else if (another.row[i] instanceof Float value2) {
                            row[i] = value1 + (value2 - value1) / (avgCounter++ + 1);
                            continue;
                        }
                    }

                    exceptions.add(new ZQLException("Grouping rule AVG contains non-number value, skipped: " + Arrays.toString(row)));
                }
                case MIN -> {
                    if (row[i] instanceof Integer value1) {
                        if (another.row[i] instanceof Integer value2) {
                            row[i] = Math.min(value1, value2);
                            continue;
                        } else if (another.row[i] instanceof Float value2) {
                            row[i] = Math.min(value1, value2);
                            continue;
                        }
                    } else if (row[i] instanceof Float value1) {
                        if (another.row[i] instanceof Integer value2) {
                            row[i] = Math.min(value1, value2);
                            continue;
                        } else if (another.row[i] instanceof Float value2) {
                            row[i] = Math.min(value1, value2);
                            continue;
                        }
                    }

                    exceptions.add(new ZQLException("Grouping rule MIN contains non-number value, skipped: " + Arrays.toString(row)));
                }
                case MAX -> {
                    if (row[i] instanceof Integer value1) {
                        if (another.row[i] instanceof Integer value2) {
                            row[i] = Math.max(value1, value2);
                            continue;
                        } else if (another.row[i] instanceof Float value2) {
                            row[i] = Math.max(value1, value2);
                            continue;
                        }
                    } else if (row[i] instanceof Float value1) {
                        if (another.row[i] instanceof Integer value2) {
                            row[i] = Math.max(value1, value2);
                            continue;
                        } else if (another.row[i] instanceof Float value2) {
                            row[i] = Math.max(value1, value2);
                            continue;
                        }
                    }

                    exceptions.add(new ZQLException("Grouping rule MAX contains non-number value, skipped: " + Arrays.toString(row)));
                }
                default -> throw new IllegalStateException("Unexpected value: " + types[i]);
            }
        }
    }

    @Override
    public int hashCode() {
        return IntStream.range(0, row.length).map(i -> types[i] == GroupingType.NONE ? row[i].hashCode() : 0).sum();
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof GroupingWrapper another && IntStream.range(0, row.length).allMatch(i -> types[i] != GroupingType.NONE || Objects.equals(row[i], another.row[i]));
    }
}
