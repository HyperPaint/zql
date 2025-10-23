package hyperpaint.zql.lang;

public class ZQLException extends RuntimeException {
    public ZQLException() {
        super();
    }

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
