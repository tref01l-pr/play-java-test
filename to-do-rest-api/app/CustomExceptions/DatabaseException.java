package CustomExceptions;

import play.mvc.Http;

public class DatabaseException extends BaseCustomException {
    private static final String DEFAULT_PROD_MESSAGE = "Database is down";

    public DatabaseException(String message) {
        super(message, DEFAULT_PROD_MESSAGE, Http.Status.BAD_GATEWAY);
    }

    public DatabaseException(String message, String prodMessage) {
        super(message, prodMessage, Http.Status.BAD_GATEWAY);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, DEFAULT_PROD_MESSAGE, Http.Status.BAD_GATEWAY, cause);
    }

    public DatabaseException(String message, String prodMessage, Throwable cause) {
        super(message, prodMessage, Http.Status.BAD_GATEWAY, cause);
    }
}