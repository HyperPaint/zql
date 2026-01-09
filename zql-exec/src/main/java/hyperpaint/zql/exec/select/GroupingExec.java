package hyperpaint.zql.exec.select;

import hyperpaint.zql.lang.condition.Condition;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class GroupingExec {
    private final GroupingType[] columnGroupingTypes;

    private final @Getter boolean skip;

    private GroupingExec(GroupingType[] columnGroupingTypes) {
        this.skip = Arrays.stream(columnGroupingTypes).allMatch(GroupingType::isNotGrouping);

        if (skip) {
            this.columnGroupingTypes = null;
        } else {
            this.columnGroupingTypes = columnGroupingTypes;
        }
    }

    public static GroupingExec get(GroupingType[] columnGroupingTypes) {
        return new GroupingExec(columnGroupingTypes);
    }

    public static void groupRows(GroupingExec exec, List<Object[]> rows) {
        if (exec.skip) {
            return;
        }

        final Map<GroupingContainer, GroupingContainer> groups = new HashMap<>();
        final GroupingContainer current = new GroupingContainer(exec.columnGroupingTypes);

        rows.removeIf(row -> {
            current.setRow(row);

            GroupingContainer buff = groups.get(current);
            if (buff != null) {
                buff.add(current);
                return true;
            } else {
                buff = new GroupingContainer(current.getRow(), exec.columnGroupingTypes);
                groups.put(buff, buff);
                return false;
            }
        });
    }
}
