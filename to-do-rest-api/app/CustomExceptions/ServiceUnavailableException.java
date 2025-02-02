package CustomExceptions;

public class ServiceUnavailableException extends RuntimeException {
    private final int statusCode = 503;
    private final String prodMessage = "Service is unavailable";

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getProdMessage() {
        return prodMessage;
    }
}