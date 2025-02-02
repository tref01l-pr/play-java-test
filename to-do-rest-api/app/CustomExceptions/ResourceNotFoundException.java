package CustomExceptions;

import play.mvc.Http;

public class ResourceNotFoundException extends BaseCustomException {
    private static final String DEFAULT_PROD_MESSAGE = "Requested resource was not found";

    public ResourceNotFoundException(String message) {
        super(message, DEFAULT_PROD_MESSAGE, Http.Status.NOT_FOUND);
    }

    public ResourceNotFoundException(String message, String prodMessage) {
        super(message, prodMessage, Http.Status.NOT_FOUND);
    }

    public ResourceNotFoundException(String message, String prodMessage, Throwable cause) {
        super(message, prodMessage, Http.Status.NOT_FOUND, cause);
    }
}