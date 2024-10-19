package exception;

public class GraphQLRequestException extends RuntimeException {
  public GraphQLRequestException(String message) {
    super(message);
  }
}
