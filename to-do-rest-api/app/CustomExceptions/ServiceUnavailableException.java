package CustomExceptions;

import play.mvc.Http;

public class ServiceUnavailableException extends BaseCustomException {
    private static final String DEFAULT_PROD_MESSAGE = "Service is unavailable";

    public ServiceUnavailableException(String message) {
        super(message, DEFAULT_PROD_MESSAGE, Http.Status.SERVICE_UNAVAILABLE);
    }

    public ServiceUnavailableException(String message, String prodMessage) {
        super(message, prodMessage, Http.Status.SERVICE_UNAVAILABLE);
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, DEFAULT_PROD_MESSAGE, Http.Status.SERVICE_UNAVAILABLE, cause);
    }

    public ServiceUnavailableException(String message, String prodMessage, Throwable cause) {
        super(message, prodMessage, Http.Status.SERVICE_UNAVAILABLE, cause);
    }
}