package exception.parser;

public class ParserException extends RuntimeException {
    public ParserException(String message, Throwable cause) {
        super(message, cause);
    }
}
