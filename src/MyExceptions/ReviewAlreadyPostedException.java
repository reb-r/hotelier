package MyExceptions;

public class ReviewAlreadyPostedException extends Exception {
    public ReviewAlreadyPostedException(int time) {
        super("Hai già fatto una recensione per questa struttura di recente!\n"
                + "Devi attendere ancora " + time + "s per pubblicare la prossima.");
    }
}


