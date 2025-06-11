public class NotValidTokenException extends Exception {
    public NotValidTokenException(String message) {
        super(message);
    }

    public NotValidTokenException() {
        super("Invalid token encountered.");
    }
}
