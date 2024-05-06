package Server.Database;

import Server.Database.Hotel.Ratings;

import java.io.Serial;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static Server.HOTELIERServer.setnReviews;


/**
 * <p align="justify">
 *     La classe {@code Review} rappresenta le recensioni pubblicate dagli utenti del servizio.
 * </p>
 */
public class Review implements Serializable {
    @Serial
    private static final long serialVersionUID = 7812427507218029179L;
    /** Formato testuale per la data */
    private static final String DATE_FORMAT = "EEE dd MMMMM yyyy HH:mm:ss z";
    /** Campi significativi della classe */
    private static final int N_FIELDS = 7;

    /** Identificatore univoco della recensione */
    private int id;
    /** Utente autore della recensione */
    private String author;
    /** Hotel recensito */
    private String hotel;
    /** Punteggio sintetico (rate) */
    private double rate;
    /** Punteggi per categoria (ratings) */
    private Ratings ratings;
    /** Data di pubblicazione */
    private String date;
    /** Utenti o guests che hanno consigliato la recensione */
    private List<String> upvotes;

    public Review(String username, String hotel, double rate, double[] ratings) {
        // Assegno alle variabili i parametri
        this.id = 1000 + setnReviews();
        this.author = username;
        this.hotel = hotel;
        this.rate = rate;
        this.ratings = new Ratings(ratings);

        // Assegno e inizializzo le altre variabili
        date = new SimpleDateFormat(DATE_FORMAT).format(new Date(System.currentTimeMillis()));
        upvotes = new LinkedList<>();
    }

    public Review() { }


    // Metodi get
    public static int getnFields() { return N_FIELDS; }

    public int getId() { return id; }

    public String getAuthor() { return author; }

    public String getHotel() { return hotel; }

    public double getRate() { return rate; }

    public double[] getRatings() { return new double[]{Double.parseDouble(ratings.toString())}; }

    public synchronized List<String> getUpvotes() { return new LinkedList<>(upvotes); }

    public synchronized int getnUpvotes() { return upvotes.size(); }

    public long getTime() {
        try {
            return new SimpleDateFormat(DATE_FORMAT).parse(date).getTime();
        } catch (ParseException ignored) {
            return 0L;
        }
    }


    @Override
    public String toString() {
        return "{" + id + "; " + author + "; " + hotel + "; " + rate + "; " + ratings.toString() + "; " + date + "; "
                + getnUpvotes() + "; " + Arrays.toString(upvotes.toArray()) + "}";
    }


    // Altri metodi
    /**
     * <p align="justify">
     *     Inserisce il voto identificato da {@code session} alla lista di voti di questa recensione.
     * </p>
     * @param session la sessione attiva corrispondente all'utente o al guest che vota la recensione
     * @return {@code true} se l'utente o guest non ha ancora votato la recensione, {@code false} altrimenti
     */
    public synchronized boolean addUpvote(String session) {
        if (upvotes.contains(session)) return false;
        return upvotes.add(session);
    }
}