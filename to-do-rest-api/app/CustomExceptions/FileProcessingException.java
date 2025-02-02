package CustomExceptions;

public class FileProcessingException extends RuntimeException {
    private final int statusCode = 400;
    private String prodMessage = "File processing failed";

    public FileProcessingException(String message) {
        super(message);
    }

    public FileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getProdMessage() {
        return prodMessage;
    }
}