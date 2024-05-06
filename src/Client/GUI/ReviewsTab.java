package Client.GUI;

import Server.Database.Review;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;

import java.awt.*;

import static Client.GUI.CustomFrame.*;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER;


/**
 * <p align="justify">
 *     La classe {@code ReviewsTab} estende la classe {@link CustomTab} ed è la tab dedicata alla visualizzazione delle
 *     recensioni pubblicate dall'utente loggato.
 * </p>
 */
public class ReviewsTab extends CustomTab {
    /** Panel dedicato alle recensioni */
    private final JPanel reviews_panel = new JPanel();
    /** Componente per la gestione dello scroll */
    private final JScrollPane scrollPane = new JScrollPane(reviews_panel, VERTICAL_SCROLLBAR_NEVER, HORIZONTAL_SCROLLBAR_NEVER);
    /** Etichetta informativa */
    private final JLabel info_label = new JLabel("Al momento non hai pubblicato ancora nessuna recensione.");

    public ReviewsTab(CustomFrame parent) {
        // Chiamo il costruttore della superclasse
        super(parent);

        // Imposto le preferenze delle variabili e gli elementi aggiuntivi
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 10));
        scrollPane.putClientProperty(FlatClientProperties.STYLE, "borderWidth:0");
        scrollPane.getVerticalScrollBar().setUnitIncrement(15);
        reviews_panel.setLayout(new BoxLayout(reviews_panel, BoxLayout.PAGE_AXIS));
        reviews_panel.setBorder(BorderFactory.createEmptyBorder(15, 10, 0, 10));
        reviews_panel.setBackground(GRAY_COLOR);

        // Definisco i componenti da aggiungere
        JLabel title_label = new JLabel("Le mie recensioni", JLabel.LEFT);
        title_label.setFont(h0_font);

        // Aggiungo tutto al panel principale di base
        add(title_label, BorderLayout.PAGE_START);
        add(scrollPane, BorderLayout.CENTER);
        add(scrollPane.getVerticalScrollBar(), BorderLayout.LINE_END);
    }


    @Override
    void clearAll() {
        if (info_label.getParent() == this) scrollPane.remove(info_label);
        // Rimuovo tutti i panels riferiti a una recensione
        else reviews_panel.removeAll();
        frame.requestFocusInWindow();
    }


    /**
     * <p align="justify">
     *     Mostra le recensioni indicate nella tab.
     * </p>
     * @param reviews la lista di recensioni pubblicate dall'utente loggato da visualizzare
     */
    public void displayReviews(String[] reviews) {
        // Prima di visualizzare le nuove recensioni pulisco il panel dedicato
        clearAll();
        if (reviews == null) return;
        // Aggiungo ogni recensione al panel dedicato
        if (reviews.length > 0) for (String result: reviews) addReview(result);
        else reviews_panel.add(info_label);
        if (reviews.length == 1) reviews_panel.add(Box.createVerticalStrut(120));
        else reviews_panel.add(Box.createVerticalGlue());
    }

    /**
     * <p align="justify">
     *     Aggiunge la recensione indicata alla lista di recensioni
     * </p>
     * @param review la recensione da aggiungere
     */
    private void addReview(String review) {
        // Identifico i singoli campi suddividendo la stringa
        String[] element = review.replaceAll("[{|}]", "").split(";");

        // Definisco il panel per la recensione e imposto le preferenze
        JPanel review_panel = new JPanel();
        review_panel.setLayout(new BoxLayout(review_panel, BoxLayout.LINE_AXIS));
        review_panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        review_panel.setPreferredSize(new Dimension(550, 150));
        review_panel.putClientProperty(FlatClientProperties.STYLE, "arc:20");

        // Definisco il panel dedicato alle informazioni generali e imposto le preferenze
        JPanel info_panel = new JPanel();
        info_panel.setLayout(new BoxLayout(info_panel, BoxLayout.PAGE_AXIS));
        info_panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        info_panel.setAlignmentX(LEFT_ALIGNMENT);
        // Definisco il panel dedicato ai punteggi per categoria e imposto le preferenze
        JPanel ratings_panel = new JPanel();
        ratings_panel.setLayout(new BoxLayout(ratings_panel, BoxLayout.PAGE_AXIS));
        ratings_panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 80));
        ratings_panel.setAlignmentX(LEFT_ALIGNMENT);

        // Aggiungo i due panel al panel della recensione
        review_panel.add(info_panel);
        review_panel.add(ratings_panel);

        for (int field = 0; field < Review.getnFields(); field++) {
            switch (field) {
                // (ID -> case 0)
                // AUTORE -> case 1, in questo caso non è necessario mostrare essendo l'utente loggato
                case 2:
                    JLabel hotel_label = new JLabel(element[field].trim(), JLabel.LEFT);
                    info_panel.add(hotel_label).setFont(UIManager.getFont("h3.font"));
                    info_panel.add(Box.createVerticalStrut(3));
                    break;
                // RATE
                case 3:
                    info_panel.add(new JLabel("Punteggio complessivo")).setFont(large_font);
                    info_panel.add(addScore(element[field].trim(), "", true));
                    info_panel.add(Box.createVerticalStrut(3));
                    break;
                // RATINGS
                case 4:
                    ratings_panel.add(new JLabel("Punteggi per categoria")).setFont(large_font);
                    displayRatings(ratings_panel, element[field].replaceAll("[\\[|\\]]", "").split(","));
                    break;
                // DATA
                case 5:
                    info_panel.add(new JLabel("Data di pubblicazione")).setFont(large_font);
                    info_panel.add(new JLabel(element[field].trim())).setFont(large_font);
                    info_panel.add(Box.createVerticalStrut(3));
                    break;
                // VOTI
                case 6:
                    info_panel.add(new JLabel("Consigliata da " + element[field].trim() + " persone"));
                    break;
                default: break;
            }
        }

        // Aggiungo il panel della recensione al panel di tutte le recensioni
        reviews_panel.add(review_panel);
        reviews_panel.add(Box.createVerticalStrut(15));
        frame.pack();
    }
}