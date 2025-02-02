package CustomExceptions;

public class DatabaseException extends RuntimeException {
    private final int statusCode = 502;
    private final String prodMessage = "Database is down";

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getProdMessage() {
        return prodMessage;
    }
}
