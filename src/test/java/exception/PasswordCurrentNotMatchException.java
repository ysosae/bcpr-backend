package exception;

public class PasswordCurrentNotMatchException extends RuntimeException {
  public PasswordCurrentNotMatchException(String message) {
    super(message);
  }
}
