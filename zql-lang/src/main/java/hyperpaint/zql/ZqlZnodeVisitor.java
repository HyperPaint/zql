package hyperpaint.zql;

import hyperpaint.zql.antlr4.ZQLBaseVisitor;
import hyperpaint.zql.antlr4.ZQLParser;
import hyperpaint.zql.znode.AbstractZnode;
import hyperpaint.zql.znode.Znode;
import hyperpaint.zql.znode.ZnodeType;

class ZqlZnodeVisitor extends ZQLBaseVisitor<AbstractZnode> {
    static final ZqlZnodeVisitor INSTANCE = new ZqlZnodeVisitor();

    @Override
    public AbstractZnode visitZnodePath(ZQLParser.ZnodePathContext ctx) {
        final String payload = ctx.TEXT().getText().substring(1, ctx.TEXT().getText().length() - 1);
        return new Znode(ZnodeType.PATH, payload);
    }
}
