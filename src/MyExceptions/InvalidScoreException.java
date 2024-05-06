package MyExceptions;

public class InvalidScoreException extends Exception {
    public InvalidScoreException(String msg) { super(msg + "\nIl voto deve essere compreso tra 0 e 5."); }
}


