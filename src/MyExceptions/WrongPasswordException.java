package MyExceptions;

public class WrongPasswordException extends Exception {
    public WrongPasswordException() { super("La password inserita è errata!"); }
}


