package hyperpaint.zql;

import hyperpaint.zql.antlr4.ZQLBaseVisitor;
import hyperpaint.zql.antlr4.ZQLParser;
import hyperpaint.zql.znode.AbstractZnode;
import hyperpaint.zql.znode.ZnodeList;
import hyperpaint.zql.znode.ZnodeType;
import hyperpaint.zql.znode.ZnodeWrapper;

import java.util.ArrayList;
import java.util.List;

class ZqlZnodesVisitor extends ZQLBaseVisitor<AbstractZnode> {
    static final ZqlZnodesVisitor INSTANCE = new ZqlZnodesVisitor();

    @Override
    public AbstractZnode visitZnodesCommaZnodes(ZQLParser.ZnodesCommaZnodesContext ctx) {
        final AbstractZnode left = visit(ctx.znodes(0));
        final AbstractZnode right = visit(ctx.znodes(1));

        if (left.getType() == ZnodeType.COMMA && right.getType() == ZnodeType.COMMA) {
            final ZnodeList leftList = (ZnodeList) left;
            final ZnodeList rightList = (ZnodeList) right;
            leftList.getList().addAll(rightList.getList());
            return leftList;
        } else if (left.getType() == ZnodeType.COMMA) {
            final ZnodeList leftList = (ZnodeList) left;
            leftList.getList().add(right);
            return leftList;
        } else if (right.getType() == ZnodeType.COMMA) {
            final ZnodeList rightList = (ZnodeList) right;
            rightList.getList().add(left);
            return rightList;
        } else {
            final List<AbstractZnode> list = new ArrayList<>();
            list.add(left);
            list.add(right);
            return new ZnodeList(ZnodeType.COMMA, list);
        }
    }

    @Override
    public AbstractZnode visitZnodesList(ZQLParser.ZnodesListContext ctx) {
        return new ZnodeWrapper(ZnodeType.LIST, ZqlZnodeVisitor.INSTANCE.visit(ctx.znode()));
    }

    @Override
    public AbstractZnode visitZnodesBase(ZQLParser.ZnodesBaseContext ctx) {
        return ZqlZnodeVisitor.INSTANCE.visit(ctx.znode());
    }
}
