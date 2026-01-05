package hyperpaint.zql.exec.select;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class GroupingExec {
    private final GroupingType[] columnGroupingTypes;
    private final List<Exception> exceptions;

    private final boolean skip;

    // todo rewrite to funcs
    GroupingExec(GroupingType[] columnGroupingTypes, List<Exception> exceptions) {
        this.columnGroupingTypes = columnGroupingTypes;
        this.exceptions = exceptions;

        skip = Arrays.stream(columnGroupingTypes).allMatch(type -> type == GroupingType.NONE);
    }

    void execute(List<Object[]> rows) {
        if (skip) {
            return;
        }

        final Map<GroupingWrapper, GroupingWrapper> groups = new HashMap<>();
        final GroupingWrapper current = new GroupingWrapper(columnGroupingTypes, exceptions);

        rows.removeIf(row -> {
            current.setRow(row);

            GroupingWrapper buff = groups.get(current);
            if (buff != null) {
                buff.add(current);
                return true;
            } else {
                buff = new GroupingWrapper(current.getRow(), columnGroupingTypes, exceptions);
                groups.put(buff, buff);
                return false;
            }
        });
    }
}
