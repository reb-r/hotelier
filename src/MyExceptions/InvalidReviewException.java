package MyExceptions;

public class InvalidReviewException extends Exception {
    public InvalidReviewException() { super("Recensione non valida!"); }
}