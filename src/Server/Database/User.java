package Server.Database;

import static Server.Database.User.Badge.*;
import static Server.HOTELIERServer.setnUsers;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;


/**
 * <p align="justify">
 *     La classe {@code User} rappresenta gli utenti del servizio.
 * </p>
 */
public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 4123916270202976310L;

    /** Identificatore univoco dell'utente */
    private int id;
    /** Nome utente */
    private String username;
    /** Password */
    private String password;
    /** Recensioni pubblicate dall'utente */
    private LinkedList<Review> reviews; // in ordine decrescente (dalla più recente alla meno recente)
    /** Badges ottenuti dall'utente */
    private LinkedList<String> badges; // in ordine decrescente (dal più recente/livello elevato al meno recente/basso)

    public User(String username, String password) {
        // Assegno alle variabili i parametri
        this.id = 1000 + setnUsers();
        this.username = username;
        this.password = password;

        // Assegno e inizializzo le altre variabili
        reviews = new LinkedList<>();
        badges = new LinkedList<>();
    }

    public User() { }


    // Metodi get
    public int getId() { return id; }

    public String getUsername() { return username; }

    public synchronized List<Review> getReviews() { return new LinkedList<>(reviews); }

    /**
     * <p align="justify">
     *     Restituisce l'ultimo badge ottenuto da questo utente, corrispondente al massimo livello raggiunto in base
     *     al numero di recensioni pubblicate, o {@code null} se non sono ancora state pubblicate recensioni.
     * </p>
     * @return l'ultimo badge ottenuto
     */
    public synchronized String getBadge() {
        if (badges.isEmpty()) return "";
        else return badges.getFirst();
    }


    // Altri metodi
    /**
     * <p align="justify">
     *     Verifica se la password inserita corrisponde a quella di questo utente.
     * </p>
     * @param password una stringa di caratteri
     * @return {@code true} se la password è corretta, {@code false} altrimenti
     */
    public boolean verifyPassword(String password) {
        return this.password.equals(password);
    }

    /**
     * <p align="justify">
     *     Aggiunge la recensione specificata alla lista di recensioni pubblicate da questo utente.
     * </p>
     *
     * @param review la recensione pubblicata
     */
    public synchronized void addReview(Review review) {
        reviews.addFirst(review);
        // Dopo l'inserimento aggiorno i badges
        updateBadges();
    }

    /**
     * <p align="justify">
     *     Aggiorna i badge di questo utente in base al numero di recensioni pubblicate.
     * </p>
     */
    private void updateBadges() {
        // Aggiungo il nuovo badge solo nel caso in cui sia stata raggiunta la soglia minima per il nuovo livello e lo
        // inserisco in testa, così da mantenere l'ordine decrescente, corrispondente al massimo livello raggiunto
        int size = reviews.size();
        if (size == RECENSORE.min) badges.addFirst(RECENSORE.badge);
        if (size == RECENSORE_ESPERTO.min) badges.addFirst(RECENSORE_ESPERTO.badge);
        if (size == CONTRIBUTORE.min) badges.addFirst(CONTRIBUTORE.badge);
        if (size == CONTRIBUTORE_ESPERTO.min) badges.addFirst(CONTRIBUTORE_ESPERTO.badge);
        if (size == CONTRIBUTORE_SUPER.min) badges.addFirst(CONTRIBUTORE_SUPER.badge);
    }


    /**
     * <p align="justify">
     *     La classe {@code Badge} rappresenta i distintivi assegnabili a ciascun utente {@code User}, in base al livello
     *     di esperienza raggiunto, determinato da un numero minimo di recensioni {@code Review} pubblicate.
     * </p>
     * @see #RECENSORE
     * @see #RECENSORE_ESPERTO
     * @see #CONTRIBUTORE
     * @see #CONTRIBUTORE_ESPERTO
     * @see #CONTRIBUTORE_SUPER
     */
    public enum Badge implements Serializable {
        /** Recensore, livello 1 */
        RECENSORE("Recensore", 1, 1),
        /** Recensore esperto, livello 2 */
        RECENSORE_ESPERTO("Recensore esperto", 2, 2),
        /** Contributore, livello 3 */
        CONTRIBUTORE("Contributore", 3, 3),
        /** Contributore esperto, livello 4 */
        CONTRIBUTORE_ESPERTO("Contributore esperto", 4, 5),
        /** Contributore super, livello 5 */
        CONTRIBUTORE_SUPER("Contributore super", 5, 8);

        final String badge; // distintivo
        final int level; // livello corrispondente al distintivo
        final int min; // numero minimo di recensioni per raggiungere il livello corrispondente

        Badge(String badge, int level, int minimum) {
            this.badge = badge;
            this.level = level;
            this.min = minimum;
        }

        @Override
        public String toString() { return badge; }

        public Badge parseBadge(String constant) {
            for (Badge badge: Badge.values()) if (badge.toString().equals(constant)) return badge;
            return null;
        }
    }
}