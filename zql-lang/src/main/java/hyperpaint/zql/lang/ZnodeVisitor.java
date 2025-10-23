package hyperpaint.zql.lang;

import hyperpaint.zql.lang.antlr4.ZQLBaseVisitor;
import hyperpaint.zql.lang.antlr4.ZQLParser;
import hyperpaint.zql.lang.znode.Znode;
import hyperpaint.zql.lang.znode.ZnodeCollection;
import hyperpaint.zql.lang.znode.ZnodePath;
import hyperpaint.zql.lang.znode.ZnodeWrapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ZnodeVisitor extends ZQLBaseVisitor<Znode> {
    static final ZnodeVisitor INSTANCE = new ZnodeVisitor();

    @Override
    public Znode visitZnodesCommaZnodes(ZQLParser.ZnodesCommaZnodesContext ctx) {
        final Znode left = visit(ctx.znodes(0));
        final Znode right = visit(ctx.znodes(1));

        if (left.getType() == Znode.Type.COMMA && right.getType() == Znode.Type.COMMA) {
            final ZnodeCollection leftCollection = (ZnodeCollection) left;
            final ZnodeCollection rightCollection = (ZnodeCollection) right;
            leftCollection.getCollection().addAll(rightCollection.getCollection());
            return leftCollection;
        } else if (left.getType() == Znode.Type.COMMA) {
            final ZnodeCollection leftCollection = (ZnodeCollection) left;
            leftCollection.getCollection().add(right);
            return leftCollection;
        } else if (right.getType() == Znode.Type.COMMA) {
            final ZnodeCollection rightCollection = (ZnodeCollection) right;
            rightCollection.getCollection().add(left);
            return rightCollection;
        } else {
            final List<Znode> list = new ArrayList<>();
            list.add(left);
            list.add(right);
            return new ZnodeCollection(list);
        }
    }

    @Override
    public Znode visitZnodesList(ZQLParser.ZnodesListContext ctx) {
        return new ZnodeWrapper(Znode.Type.LIST, visit(ctx.znodes()));
    }

    @Override
    public Znode visitZnodesBase(ZQLParser.ZnodesBaseContext ctx) {
        return visit(ctx.znode());
    }

    @Override
    public Znode visitZnode(ZQLParser.ZnodeContext ctx) {
        return new ZnodePath(ctx.getText());
    }
}
