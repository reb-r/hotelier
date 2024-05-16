package MyExceptions;

public class UsernameAlreadyTakenException extends Exception {
    public UsernameAlreadyTakenException() { super("Il nome utente inserito è già in uso! Riprova o effettua l'accesso."); }
}