package Server.Database.Hotel;

import java.io.Serial;
import java.io.Serializable;


/**
 * La classe {@code Ratings} rappresenta l'insieme dei voti di categoria relativi a un hotel.
 */
public class Ratings implements Serializable {
    @Serial
    private static final long serialVersionUID = 2487710823238021313L;
    /** Campi significativi della classe */
    private static final int N_CATEGORIES = 4;

    /** Posizione */
    private double location;
    /** Pulizia */
    private double cleanliness;
    /** Servizio */
    private double service;
    /** Qualità/prezzo */
    private double value;

    public Ratings() {
        location = 0;
        cleanliness = 0;
        service = 0;
        value = 0;
    }

    public Ratings(double[] ratings) {
        this();
        if (ratings.length != 4) return;
        location = ratings[0];
        cleanliness = ratings[1];
        service = ratings[2];
        value = ratings[3];
    }


    // Metodi get e set
    public static int getnCategories() { return N_CATEGORIES; }


    @Override
    public String toString() {
        return "[" + location + ", " + cleanliness + ", " + service + ", " + value + "]";
    }


    // Altri metodi
    /**
     * <p align="justify">
     *     Aggiorna i valori per ciascuna categoria con i voti indicati.
     * </p>
     * @param scores i voti con cui aggiornare i punteggi per categoria
     * @param size il numero totale di voti
     */
    public void updateRatings(double[] scores, int size) {
        location = calculateAverage(location, scores[0], size);
        cleanliness = calculateAverage(cleanliness, scores[1], size);
        service = calculateAverage(service, scores[2], size);
        value = calculateAverage(value, scores[3], size);
    }

    /**
     * <p align="justify">
     *     Calcola la media aritmetica a partire dal valore corrente di questa con l'aggiunta di un nuovo voto.
     * </p>
     * @param curr_average il valore corrente della media
     * @param score il voto da aggiungere alla media
     * @param count il numero totale di voti
     * @return il valore che corrisponde alla nuova media corrente
     */
    private double calculateAverage(double curr_average, double score, int count) {
        double average = ((count - 1) * curr_average + score)/count; // calcolo la media con l'aggiunta del nuovo rating
        return (double) Math.round(average * 10)/10.0; // arrotondamento della media a una cifra decimale
    }
}