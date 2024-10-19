package exception;

public class ClaimAlreadyExistException extends RuntimeException {
  public ClaimAlreadyExistException(String message) {
    super(message);
  }
}
