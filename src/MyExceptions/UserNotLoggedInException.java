package MyExceptions;

public class UserNotLoggedInException extends Exception {
    public UserNotLoggedInException() { super("L'utente non ha effettuato l'accesso!"); }
}
