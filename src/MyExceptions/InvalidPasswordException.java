package MyExceptions;

public class InvalidPasswordException extends Exception {
    public InvalidPasswordException() { super("Password non valida!"); }
}