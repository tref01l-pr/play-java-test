package CustomExceptions;

public class InvalidRequestException extends RuntimeException {
  private final int statusCode = 400;
  private final String prodMessage = "Invalid request";

  public InvalidRequestException(String message) {
    super(message);
  }

  public int getStatusCode() {
    return statusCode;
  }

    public String getProdMessage() {
        return prodMessage;
    }
}