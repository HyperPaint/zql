package hyperpaint.zql.lang;

import hyperpaint.zql.lang.antlr4.ZQLErrorListener;
import hyperpaint.zql.lang.antlr4.ZQLLexer;
import hyperpaint.zql.lang.antlr4.ZQLParser;
import hyperpaint.zql.lang.statement.Statement;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ZQL {
    public static Statement parse(String query) throws ZQLException {
        final ZQLErrorListener errorListener = new ZQLErrorListener();

        try {
            final CharStream charStream = CharStreams.fromString(query);
            final ZQLLexer lexer = new ZQLLexer(charStream);
            lexer.addErrorListener(errorListener);

            final TokenStream tokenStream = new CommonTokenStream(lexer);
            final ZQLParser parser = new ZQLParser(tokenStream);
            parser.addErrorListener(errorListener);

            final ParseTree parseTree = parser.zql();
            return ZQLVisitor.INSTANCE.visit(parseTree);
        } catch (Exception e) {
            throw new ZQLException(e);
        } finally {
            errorListener.throwIfNotEmpty();
        }
    }
}
