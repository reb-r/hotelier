package MyExceptions;

public class InvalidVoteException extends Exception {
    public InvalidVoteException() { super("Voto non valido! Sei autore di questa recensione.\n"
            + "Gli autori non possono votare le proprie recensioni."); }
}


