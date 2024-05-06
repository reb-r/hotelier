package Client.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static Client.GUI.CustomFrame.*;
import static Server.Database.User.Badge.*;


/**
 * <p align="justify">
 *     La classe {@code BadgePanel} estende la classe {@link CustomTab} ed è la tab dedicata alla visualizzazione delle
 *     informazioni relative al badge dell'utente loggato.
 * </p>
 */
public class BadgeTab extends CustomTab {
    /** Parent panel */
    final SessionPanel panel;
    /** Etichetta per il badge */
    private final JLabel badge_label = new JLabel("", JLabel.LEFT);
    /** Etichetta per le informazioni sul badge */
    private final JLabel info_label = new JLabel("", JLabel.LEFT);
    /** Etichetta per tornare alla home */
    private final JLabel home_label = new JLabel("Torna alla home e pubblica la tua prima recensione.", JLabel.LEFT);
    /** Panel dedicato per informazioni aggiuntive */
    private final JPanel info_panel = new JPanel(new BorderLayout(5, 0));

    public BadgeTab(CustomFrame parent, SessionPanel custom_parent) {
        // Chiamo il costruttore della superclasse e assegno i parametri
        super(parent);
        panel = custom_parent;

        // Imposto le preferenze delle variabili e gli elementi aggiuntivi
        setLayout(new BorderLayout(0, 20));
        setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 10));
        info_panel.setMaximumSize(new Dimension(600, 200));
        info_panel.setAlignmentX(LEFT_ALIGNMENT);
        badge_label.setFont(h2_regular_font);
        info_label.setFont(h3_regular_font);
        home_label.setFont(large_font);
        setUnderline(home_label).addMouseListener(new HomeMouseAdapter());

        // Gestisco le variabili componenti
        info_panel.add(Box.createVerticalStrut(5), BorderLayout.PAGE_START);
        info_panel.add(info_label, BorderLayout.CENTER);

        // Definisco i componenti nel panel
        // Definisco i componenti da aggiungere
        JLabel title_label = new JLabel("Il mio badge", JLabel.LEFT);
        title_label.setFont(h0_font);

        // Definisco il panel ausiliario e imposto le preferenze
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
        container.setMaximumSize(new Dimension(600, 300));
        // Aggiungo i componenti al panel ausiliario
        container.add(badge_label);
        container.add(info_panel);
        container.add(Box.createVerticalGlue());

        // Aggiungo tutto al panel principale di base
        add(title_label, BorderLayout.PAGE_START);
        add(container, BorderLayout.CENTER);
    }


    @Override
    void clearAll() {
        if (home_label.getParent() == info_panel) info_panel.remove(home_label);
        badge_label.setText("");
        info_label.setText("");
    }


    /**
     * <p align="justify">
     *     Mostra il badge indicato nella pagina e ulteriori informazioni per indirizzare l'utente.
     * </p>
     * @param badge il badge dell'utente loggato da visualizzare
     */
    void displayBadges(String badge) {
        badge_label.setText("Il tuo livello attuale è: " + badge + ".");
        if (badge.equals("N/A")) { // se il distintivo non è disponibile
            // Mostro la scritta corrispondente e aggiungo al panel l'etichetta per tornare alla home
            info_label.setText("<html>Condividi le tue esperienze con gli altri utenti!</html>");
            info_panel.add(home_label, BorderLayout.PAGE_END);
        } else {
            // Controllo se l'etichetta per tornare alla home è ancora presente e la rimuovo
            if (home_label.getParent() == info_panel) info_panel.remove(home_label);
            // Mostro la scritta corrispondente in base al livello raggiunto
            if (badge.equals(CONTRIBUTORE_SUPER.toString()))
                info_label.setText("<html>Congratulazioni! Hai raggiunto il livello massimo e fai parte dei nostri contributori più attivi!</html>");
            else info_label.setText("<html>Congratulazioni! Continua così per salire di livello e ottenere nuovi distintivi.</html>");
            frame.requestFocusInWindow();
        }
    }


    // Elementi per la gestione dei componenti durante l'interazione
    /**
     * <p align="justify">
     *     {@link MouseAdapter} per l'etichetta di ritorno alla home
     * </p>
     * Consente il ritorno alla home.
     */
    class HomeMouseAdapter extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            panel.setSelectedTab(0);
        }
    }
}