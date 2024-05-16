package MyExceptions;

public class UserNotRegisteredException extends Exception {
    public UserNotRegisteredException() { super("Utente non registrato!"); }
}