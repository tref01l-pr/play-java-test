package CustomExceptions;

import play.mvc.Http;

public class InvalidRequestException extends BaseCustomException {
  private static final String DEFAULT_PROD_MESSAGE = "Invalid request";

  public InvalidRequestException(String message) {
    super(message, DEFAULT_PROD_MESSAGE, Http.Status.BAD_REQUEST);
  }

  public InvalidRequestException(String message, String prodMessage) {
    super(message, prodMessage, Http.Status.BAD_REQUEST);
  }

  public InvalidRequestException(String message, Throwable cause) {
    super(message, DEFAULT_PROD_MESSAGE, Http.Status.BAD_REQUEST, cause);
  }

  public InvalidRequestException(String message, String prodMessage, Throwable cause) {
    super(message, prodMessage, Http.Status.BAD_REQUEST, cause);
  }
}