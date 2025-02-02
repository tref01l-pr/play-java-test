package CustomExceptions;

public class ValidationException extends RuntimeException {
    private final int statusCode = 400;
    private final String prodMessage = "The file validation failed";

    public ValidationException(String message) {
        super(message);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getProdMessage() {
        return prodMessage;
    }
}