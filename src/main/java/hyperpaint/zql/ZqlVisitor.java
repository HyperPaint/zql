package hyperpaint.zql;

import hyperpaint.zql.antlr4.ZQLBaseVisitor;
import hyperpaint.zql.antlr4.ZQLParser;
import hyperpaint.zql.statement.Statement;

public class ZqlVisitor extends ZQLBaseVisitor<Statement> {
    public static final ZqlVisitor INSTANCE = new ZqlVisitor();

    @Override
    public Statement visitZql(ZQLParser.ZqlContext ctx) {
        return ZqlStatementVisitor.INSTANCE.visit(ctx.statement());
    }
}
