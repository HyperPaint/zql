package hyperpaint.zql.lang;

import hyperpaint.zql.lang.antlr4.ZQLBaseVisitor;
import hyperpaint.zql.lang.antlr4.ZQLParser;
import hyperpaint.zql.lang.znode.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ZnodeVisitor extends ZQLBaseVisitor<Znode> {
    static final ZnodeVisitor INSTANCE = new ZnodeVisitor();

    @Override
    public Znode visitZnodesComma(ZQLParser.ZnodesCommaContext ctx) {
        final Znode left = visit(ctx.getChild(0));
        final Znode right = visit(ctx.getChild(2));

        if (left.getType() == Znode.Type.COLLECTION && right.getType() == Znode.Type.COLLECTION) {
            final ZnodeCollection leftCollection = (ZnodeCollection) left;
            final ZnodeCollection rightCollection = (ZnodeCollection) right;
            leftCollection.getList().addAll(rightCollection.getList());
            return leftCollection;
        } else if (left.getType() == Znode.Type.COLLECTION) {
            final ZnodeCollection leftCollection = (ZnodeCollection) left;
            leftCollection.getList().add(right);
            return leftCollection;
        } else if (right.getType() == Znode.Type.COLLECTION) {
            final ZnodeCollection rightCollection = (ZnodeCollection) right;
            rightCollection.getList().add(left);
            return rightCollection;
        } else {
            final List<Znode> list = new ArrayList<>();
            list.add(left);
            list.add(right);
            return new ZnodeCollection(list);
        }
    }

    @Override
    public Znode visitZnodesBase(ZQLParser.ZnodesBaseContext ctx) {
        return visit(ctx.znode());
    }

    @Override
    public Znode visitZnode(ZQLParser.ZnodeContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public Znode visitZnodeList(ZQLParser.ZnodeListContext ctx) {
        return new ZnodeList(visit(ctx.children.getLast()));
    }

    @Override
    public Znode visitZnodePath(ZQLParser.ZnodePathContext ctx) {
        return new ZnodePath(ctx.getText());
    }

    @Override
    public Znode visitZnodeRoot(ZQLParser.ZnodeRootContext ctx) {
        return new ZnodePath(ctx.getText());
    }
}
