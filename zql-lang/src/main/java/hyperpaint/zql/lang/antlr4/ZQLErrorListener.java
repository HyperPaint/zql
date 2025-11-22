package hyperpaint.zql.lang.antlr4;

import hyperpaint.zql.lang.ZQLException;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

import java.util.BitSet;
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
            exceptions.add(new ZQLException("Can't parse ZQL statement, unknown token at line " + line + " at position " + (charPositionInLine + 1) + ", " + msg, e));
        }
    }

    @Override
    public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {

    }

    @Override
    public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {

    }

    @Override
    public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {

    }

    public void throwIfNotEmpty() throws ZQLException {
        if (!exceptions.isEmpty()) {
            throw exceptions.getFirst();
        }
    }
}
