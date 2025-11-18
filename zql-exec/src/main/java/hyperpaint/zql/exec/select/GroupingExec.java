package hyperpaint.zql.exec.select;

import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class GroupingExec {
    @FunctionalInterface
    public interface RowGrouping {
        void run(List<Object[]> rows);
    }

    @NoArgsConstructor
    class Wrapper {
        private Object[] row;
        private int consumed = 0;

        public Wrapper(Object[] row) {
            this.row = row;
        }

        public void consume(Wrapper another) {
            final Object[] anotherRow = another.row;

            for (int i = 0; i < row.length; i++) {
                switch (columnsExpressionType[i]) {
                    case COUNT -> {
                        if (row[i] instanceof Integer value) {
                            row[i] = value + 1;
                        } else {
                            // todo exception
                        }
                    }
                    case SUM -> {
                        if (row[i] instanceof Integer value1) {
                            if (anotherRow[i] instanceof Integer value2) {
                                row[i] = value1 + value2;
                            } else if (anotherRow[i] instanceof Float value2) {
                                row[i] = value1 + value2;
                            } else {
                                // todo exception
                            }
                        } else if (row[i] instanceof Float value1) {
                            if (anotherRow[i] instanceof Integer value2) {
                                row[i] = value1 + value2;
                            } else if (anotherRow[i] instanceof Float value2) {
                                row[i] = value1 + value2;
                            } else {
                                // todo exception
                            }
                        } else {
                            // todo exception
                        }
                    }
                    case AVG -> {
                        if (row[i] instanceof Integer value1) {
                            if (anotherRow[i] instanceof Integer value2) {
                                row[i] = value1 + (value2 - value1) / (consumed + 1);
                            } else if (anotherRow[i] instanceof Float value2) {
                                row[i] = value1 + (value2 - value1) / (consumed + 1);
                            } else {
                                // todo exception
                            }
                        } else if (row[i] instanceof Float value1) {
                            if (anotherRow[i] instanceof Integer value2) {
                                row[i] = value1 + (value2 - value1) / (consumed + 1);
                            } else if (anotherRow[i] instanceof Float value2) {
                                row[i] = value1 + (value2 - value1) / (consumed + 1);
                            } else {
                                // todo exception
                            }
                        } else {
                            // todo exception
                        }
                    }
                    case MIN -> {
                        if (row[i] instanceof Integer value1) {
                            if (anotherRow[i] instanceof Integer value2) {
                                row[i] = Math.min(value1, value2);
                            } else if (anotherRow[i] instanceof Float value2) {
                                row[i] = Math.min(value1, value2);
                            } else {
                                // todo exception
                            }
                        } else if (row[i] instanceof Float value1) {
                            if (anotherRow[i] instanceof Integer value2) {
                                row[i] = Math.min(value1, value2);
                            } else if (anotherRow[i] instanceof Float value2) {
                                row[i] = Math.min(value1, value2);
                            } else {
                                // todo exception
                            }
                        } else {
                            // todo exception
                        }
                    }
                    case MAX -> {
                        if (row[i] instanceof Integer value1) {
                            if (anotherRow[i] instanceof Integer value2) {
                                row[i] = Math.max(value1, value2);
                            } else if (anotherRow[i] instanceof Float value2) {
                                row[i] = Math.max(value1, value2);
                            } else {
                                // todo exception
                            }
                        } else if (row[i] instanceof Float value1) {
                            if (anotherRow[i] instanceof Integer value2) {
                                row[i] = Math.max(value1, value2);
                            } else if (anotherRow[i] instanceof Float value2) {
                                row[i] = Math.max(value1, value2);
                            } else {
                                // todo exception
                            }
                        } else {
                            // todo exception
                        }
                    }
                }
            }

            consumed++;
        }

        @Override
        public int hashCode() {
            return IntStream.range(0, groupingIndex.length).map(i -> row[i].hashCode()).sum();
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof Wrapper wrapper && IntStream.range(0, groupingIndex.length).allMatch(i -> Objects.equals(row[i], wrapper.row[i]));
        }
    }
}
