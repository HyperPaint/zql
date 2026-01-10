package hyperpaint.zql.lang;

import hyperpaint.zql.lang.antlr4.ZQLBaseVisitor;
import hyperpaint.zql.lang.antlr4.ZQLParser;
import hyperpaint.zql.lang.znode.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ZNodeVisitor extends ZQLBaseVisitor<ZNode> {
    static final ZNodeVisitor INSTANCE = new ZNodeVisitor();

    @Override
    public ZNode visitZnodesComma(ZQLParser.ZnodesCommaContext ctx) {
        final ZNode left = visit(ctx.getChild(0));
        final ZNode right = visit(ctx.getChild(2));

        if (left.getType() == ZNode.Type.COLLECTION && right.getType() == ZNode.Type.COLLECTION) {
            final ZNodeCollection leftCollection = (ZNodeCollection) left;
            final ZNodeCollection rightCollection = (ZNodeCollection) right;
            leftCollection.getList().addAll(rightCollection.getList());
            return leftCollection;
        } else if (left.getType() == ZNode.Type.COLLECTION) {
            final ZNodeCollection leftCollection = (ZNodeCollection) left;
            leftCollection.getList().add(right);
            return leftCollection;
        } else if (right.getType() == ZNode.Type.COLLECTION) {
            final ZNodeCollection rightCollection = (ZNodeCollection) right;
            rightCollection.getList().add(left);
            return rightCollection;
        } else {
            final List<ZNode> list = new ArrayList<>();
            list.add(left);
            list.add(right);
            return new ZNodeCollection(list);
        }
    }

    @Override
    public ZNode visitZnodesBase(ZQLParser.ZnodesBaseContext ctx) {
        return visit(ctx.znode());
    }

    @Override
    public ZNode visitZnode(ZQLParser.ZnodeContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public ZNode visitZnodeList(ZQLParser.ZnodeListContext ctx) {
        return new ZNodeList(visit(ctx.children.getLast()));
    }

    @Override
    public ZNode visitZnodePath(ZQLParser.ZnodePathContext ctx) {
        return new ZNodePath(ctx.getText());
    }

    @Override
    public ZNode visitZnodeRoot(ZQLParser.ZnodeRootContext ctx) {
        return new ZNodePath(ctx.getText());
    }
}
