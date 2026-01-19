package hyperpaint.zql.lang;

import hyperpaint.zql.lang.antlr4.ZQLErrorListener;
import hyperpaint.zql.lang.antlr4.ZQLLexer;
import hyperpaint.zql.lang.antlr4.ZQLParser;
import hyperpaint.zql.lang.statement.Statement;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ZQL {
    public static Statement parse(String query) throws ZQLException {
        final ZQLErrorListener errorListener = new ZQLErrorListener();

        final CharStream charStream = CharStreams.fromString(query);
        final ZQLLexer lexer = new ZQLLexer(charStream);
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        final TokenStream tokenStream = new CommonTokenStream(lexer);
        final ZQLParser parser = new ZQLParser(tokenStream);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        final ParseTree parseTree = parser.zql();
        final Statement statement = ZQLVisitor.INSTANCE.visit(parseTree);

        errorListener.throwIfNotEmpty();

        return statement;
    }
}
