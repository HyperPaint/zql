package hyperpaint.zql.exec.select;

import hyperpaint.zql.lang.ZQLException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.IntStream;

@Slf4j
class GroupingContainer {
    private @Getter @Setter Object[] row;
    private final GroupingType[] types;

    private int avgCounter = 0;

    public GroupingContainer(GroupingType[] types) {
        this(null, types);
    }

    public GroupingContainer(Object[] row, GroupingType[] types) {
        this.row = row;
        this.types = types;
    }

    public void add(GroupingContainer another) {
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

                    log.warn("Grouping rule count contains non-number value, skipped: {}", Arrays.toString(row));
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

                    log.warn("Grouping rule sum contains non-number value, skipped: {}", Arrays.toString(row));
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

                    log.warn("Grouping rule avg contains non-number value, skipped: {}", Arrays.toString(row));
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

                    log.warn("Grouping rule min contains non-number value, skipped: {}", Arrays.toString(row));
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

                    log.warn("Grouping rule max contains non-number value, skipped: {}", Arrays.toString(row));
                }
                default -> throw new IllegalStateException("Unexpected value: " + types[i]);
            }
        }
    }

    @Override
    public int hashCode() {
        return IntStream.range(0, row.length).parallel().map(i -> types[i] == GroupingType.NONE ? row[i].hashCode() : 0).sum();
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof GroupingContainer another && IntStream.range(0, row.length).parallel().allMatch(i -> types[i] != GroupingType.NONE || Objects.equals(row[i], another.row[i]));
    }
}
