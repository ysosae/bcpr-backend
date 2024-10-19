package exception;

public class PasswordAlreadyExistException extends RuntimeException {
  public PasswordAlreadyExistException(String message) {
    super(message);
  }
}
