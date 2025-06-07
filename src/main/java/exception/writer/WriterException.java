package exception.writer;

public class WriterException extends RuntimeException {
    public WriterException(String message, Throwable cause) {
        super(message, cause);
    }
}
