package CustomExceptions;

import play.mvc.Http;

public class FileProcessingException extends BaseCustomException {
    private static final String DEFAULT_PROD_MESSAGE = "File processing failed";

    public FileProcessingException(String message) {
        super(message, DEFAULT_PROD_MESSAGE, Http.Status.BAD_REQUEST);
    }

    public FileProcessingException(String message, String prodMessage) {
        super(message, prodMessage, Http.Status.BAD_REQUEST);
    }

    public FileProcessingException(String message, Throwable cause) {
        super(message, DEFAULT_PROD_MESSAGE, Http.Status.BAD_REQUEST, cause);
    }

    public FileProcessingException(String message, String prodMessage, Throwable cause) {
        super(message, prodMessage, Http.Status.BAD_REQUEST, cause);
    }
}