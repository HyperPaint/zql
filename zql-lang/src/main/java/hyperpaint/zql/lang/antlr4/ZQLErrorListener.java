package hyperpaint.zql.lang.antlr4;

import hyperpaint.zql.lang.ZQLException;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

import java.util.LinkedList;
import java.util.List;

public class ZQLErrorListener extends BaseErrorListener {
    /** Ошибки возникшие во время разбора */
    private final List<ZQLException> exceptions = new LinkedList<>();

    public ZQLErrorListener() {
        super();
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        if (offendingSymbol instanceof Token token) {
            exceptions.add(new ZQLException("Can't parse ZQL statement, unexpected token \"" + token.getText() + "\" at line " + line + " at position " + (charPositionInLine + 1) + ", " + msg, e));
        } else {
            exceptions.add(new ZQLException("Can't parse ZQL statement, unexpected token at line " + line + " at position " + (charPositionInLine + 1) + ", " + msg, e));
        }
    }

    public void throwIfNotEmpty() throws ZQLException {
        if (!exceptions.isEmpty()) {
            throw exceptions.getFirst();
        }
    }
}
