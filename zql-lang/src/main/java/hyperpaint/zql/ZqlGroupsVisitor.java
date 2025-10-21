package hyperpaint.zql;

import hyperpaint.zql.antlr4.ZQLBaseVisitor;
import hyperpaint.zql.antlr4.ZQLParser;
import hyperpaint.zql.groups.AbstractGroup;
import hyperpaint.zql.groups.GroupList;
import hyperpaint.zql.groups.GroupType;

import java.util.ArrayList;
import java.util.List;

class ZqlGroupsVisitor extends ZQLBaseVisitor<AbstractGroup> {
    static final ZqlGroupsVisitor INSTANCE = new ZqlGroupsVisitor();

    @Override
    public AbstractGroup visitGroupsCommaGroups(ZQLParser.GroupsCommaGroupsContext ctx) {
        final AbstractGroup left = visit(ctx.groups(0));
        final AbstractGroup right = visit(ctx.groups(1));

        if (left.getType() == GroupType.COMMA && right.getType() == GroupType.COMMA) {
            final GroupList leftList = (GroupList) left;
            final GroupList rightList = (GroupList) right;
            leftList.getList().addAll(rightList.getList());
            return leftList;
        } else if (left.getType() == GroupType.COMMA) {
            final GroupList leftList = (GroupList) left;
            leftList.getList().add(right);
            return leftList;
        } else if (right.getType() == GroupType.COMMA) {
            final GroupList rightList = (GroupList) right;
            rightList.getList().add(left);
            return rightList;
        } else {
            final List<AbstractGroup> list = new ArrayList<>();
            list.add(left);
            list.add(right);
            return new GroupList(GroupType.COMMA, list);
        }
    }

    @Override
    public AbstractGroup visitGroupsBase(ZQLParser.GroupsBaseContext ctx) {
        return ZqlGroupVisitor.INSTANCE.visit(ctx.group());
    }
}
