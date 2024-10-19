package exception;

public class ServerDownException extends RuntimeException {
  public ServerDownException(String message) {
    super(message);
  }
}
