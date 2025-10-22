package hyperpaint.zql.lang;

public class ZqlException extends RuntimeException {
    public ZqlException(String message, int line, int column) {
        super(message + " at " + line + ":" + column);
    }

    public ZqlException(String message) {
        super(message);
    }

    public ZqlException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZqlException(Throwable cause) {
        super(cause);
    }
}
