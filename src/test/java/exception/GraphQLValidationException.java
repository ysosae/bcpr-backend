package exception;

public class GraphQLValidationException extends RuntimeException {
  public GraphQLValidationException(String message) {
    super(message);
  }
}
