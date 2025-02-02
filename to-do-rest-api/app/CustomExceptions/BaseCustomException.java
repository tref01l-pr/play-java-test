package CustomExceptions;

public abstract class BaseCustomException extends RuntimeException {
    private final int statusCode;
    private final String prodMessage;

    protected BaseCustomException(String message, String prodMessage, int statusCode) {
        super(message);
        this.statusCode = statusCode;
        this.prodMessage = prodMessage;
    }

    protected BaseCustomException(String message, String prodMessage, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.prodMessage = prodMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getProdMessage() {
        return prodMessage;
    }
}