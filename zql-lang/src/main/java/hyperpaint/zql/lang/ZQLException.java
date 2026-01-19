package hyperpaint.zql.lang;

public class ZQLException extends Exception {
    public ZQLException(String message) {
        super(message);
    }

    public ZQLException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZQLException(Throwable cause) {
        super(cause);
    }
}
