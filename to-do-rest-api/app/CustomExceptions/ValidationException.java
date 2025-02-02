package CustomExceptions;

import play.mvc.Http;

public class ValidationException extends BaseCustomException {
    private static final String DEFAULT_PROD_MESSAGE = "The file validation failed";

    public ValidationException(String message) {
        super(message, DEFAULT_PROD_MESSAGE, Http.Status.BAD_REQUEST);
    }

    public ValidationException(String message, String prodMessage) {
        super(message, prodMessage, Http.Status.BAD_REQUEST);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, DEFAULT_PROD_MESSAGE, Http.Status.BAD_REQUEST, cause);
    }

    public ValidationException(String message, String prodMessage, Throwable cause) {
        super(message, prodMessage, Http.Status.BAD_REQUEST, cause);
    }
}