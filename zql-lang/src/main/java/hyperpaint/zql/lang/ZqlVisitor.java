package hyperpaint.zql.lang;

import hyperpaint.zql.lang.antlr4.ZQLBaseVisitor;
import hyperpaint.zql.lang.antlr4.ZQLParser;
import hyperpaint.zql.lang.statement.Statement;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ZqlVisitor extends ZQLBaseVisitor<Statement> {
    public static final ZqlVisitor INSTANCE = new ZqlVisitor();

    @Override
    public Statement visitZql(ZQLParser.ZqlContext ctx) {
        return StatementVisitor.INSTANCE.visit(ctx.statement());
    }
}
