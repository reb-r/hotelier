package MyExceptions;

public class UserAlreadyLoggedInException extends Exception {
    public UserAlreadyLoggedInException() { super("L'utente ha già effettuato l'accesso!"); }
}


