package CustomExceptions;

import play.mvc.Http;

public class TokenGenerationException extends BaseCustomException {
    private static final String DEFAULT_PROD_MESSAGE = "Error generating authentication token";

    public TokenGenerationException(String message) {
        super(message, DEFAULT_PROD_MESSAGE, Http.Status.INTERNAL_SERVER_ERROR);
    }

    public TokenGenerationException(String message, String prodMessage) {
        super(message, prodMessage, Http.Status.INTERNAL_SERVER_ERROR);
    }

    public TokenGenerationException(String message, Throwable cause) {
        super(message, DEFAULT_PROD_MESSAGE, Http.Status.INTERNAL_SERVER_ERROR, cause);
    }

    public TokenGenerationException(String message, String prodMessage, Throwable cause) {
        super(message, prodMessage, Http.Status.INTERNAL_SERVER_ERROR, cause);
    }
}