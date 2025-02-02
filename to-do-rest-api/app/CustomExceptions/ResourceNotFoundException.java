package CustomExceptions;

public class ResourceNotFoundException extends RuntimeException {
    private final int statusCode = 404;
    private final String prodMessage = "Resource not found";

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getProdMessage() {
        return prodMessage;
    }
}