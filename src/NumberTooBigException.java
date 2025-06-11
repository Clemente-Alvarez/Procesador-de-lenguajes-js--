public class NumberTooBigException extends Exception {
    public NumberTooBigException(String message) {
        super(message);
    }

    public NumberTooBigException() {
        super("Number exceeds allowed size.");
    }
}
