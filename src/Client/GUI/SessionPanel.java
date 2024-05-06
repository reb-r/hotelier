package Client.GUI;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import static Client.GUI.CustomFrame.large_font;
import static javax.swing.JOptionPane.*;


/**
 * <p align="justify">
 *     La classe {@code SessionPanel} estende la classe {@link CustomPanel} e rappresenta la vista dell'applicazione da
 *     utente loggato.
 * </p>
 */
public class SessionPanel extends CustomPanel {
    /** Home tab */
    private final HomeTab home_tab;
    /** Tab per il badge */
    private final BadgeTab badge_tab;
    /** Tab per le recensioni */
    private final ReviewsTab reviews_tab;
    /** Etichetta per il logout */
    private final JLabel logout_label = new JLabel("Logout", JLabel.CENTER);
    /** Insieme di opzioni per la scelta di logout */
    private final Object[] options = {"Sì", "No"};
    /** Etichetta di sessione */
    private final JLabel session_label = new JLabel("", JLabel.CENTER);
    /** <p align="justify"> Indice della tab attiva al momento dell'invocazione dell'evento {@code ChangeEvent} </p> */
    private int activeTab = 0;

    public SessionPanel(CustomFrame parent, Dimension dimension) throws IOException, FontFormatException {
        // Chiamo il costruttore della superclasse e inizializzo le variabili
        super(parent);
        home_tab = new HomeTab(parent, dimension);

        // Imposto le preferenze delle variabili e gli elementi aggiuntivi
        tabs.setFont(large_font);
        tabs.setPreferredSize(dimension);
        logout_label.setFont(large_font);
        logout_label.setPreferredSize(new Dimension(120, 25));
        logout_label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        session_label.setFont(large_font);
        session_label.setVerticalAlignment(JLabel.BOTTOM);
        session_label.setBorder(new MatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        session_label.setPreferredSize(new Dimension(120, 25));
        defineComponentsBehavior();

        // Aggiungo le tab
        tabs.add("Home", home_tab);
        tabs.add("Cerca", search_tab);
        tabs.setEnabledAt(1, false);
        // Riempio lo spazio per visualizzare le tabs in fondo
        for (int i = 0; i < 10; i++) {
            tabs.add("", null);
            tabs.setEnabledAt(tabs.getTabCount() - 1, false);
        }
        tabs.add("Le mie recensioni", reviews_tab = new ReviewsTab(parent));
        tabs.add("Il mio badge", badge_tab = new BadgeTab(parent, this));
        tabs.add("logout", null);
        tabs.setTabComponentAt(tabs.getTabCount() - 1, logout_label);
        tabs.add("session", null);
        tabs.setTabComponentAt(tabs.getTabCount() - 1, session_label);
        tabs.setEnabledAt(tabs.getTabCount() - 1, false);
        setSelectedTab(0);

        // Aggiungo tutto al panel principale di base
        add(tabs);
    }

    private void defineComponentsBehavior() {
        tabs.addChangeListener(new TabChangeListener());
        logout_label.addMouseListener(new LogoutMouseAdapter());
    }


    @Override
    void clearAll() {
        session_label.setText("");
        home_tab.clearAll();
        search_tab.clearAll();
        reviews_tab.clearAll();
        badge_tab.clearAll();
        tabs.setSelectedIndex(0);
        tabs.setEnabledAt(1, false);
        frame.requestFocusInWindow();
    }


    /**
     * <p align="justify">
     *     Imposta il nome utente da visualizzare nella pagina.
     * </p>
     * @param session il nome utente dell'utente loggato per questa sessione
     */
    public void setSession(String session) {
        session_label.setText(session);
    }


    // Elementi per la gestione dei componenti durante l'interazione
    /**
     * {@link ChangeListener} per la selezione delle tab
     * <p align="justify">
     *     Gestisce la corretta visualizzazione delle tab selezionate.
     * </p>
     */
    class TabChangeListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            int size = tabs.getTabCount();
            // Se la tab selezionata è la penultima (quindi quella di logout) ignoro la selezione
            if (tabs.getSelectedIndex() != size - 2) {
                // Visualizzo di conseguenza gli elementi richiesti in base alla tab selezionata
                if (tabs.getSelectedIndex() == size - 4) reviews_tab.displayReviews(frame.showMyReviewsClient());
                if (tabs.getSelectedIndex() == size - 3) badge_tab.displayBadges(frame.showMyBadgesClient());
                // Se la tab selezionata è diversa dalla prima, pulisco la home
                if (tabs.getSelectedIndex() != 0) home_tab.clearAll();
                // Imposto la tab attiva come la corrente
                activeTab = tabs.getSelectedIndex();
            } else tabs.setSelectedIndex(activeTab);
            frame.requestFocusInWindow();
        }
    }

    /**
     * {@link MouseAdapter} per il pulsante di logout
     * <p align="justify">
     *     Gestisce il comportamento del pulsante corrispondente.
     * </p>
     */
    class LogoutMouseAdapter extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            // Chiedo conferma della scelta
            int choice = showOptionDialog(frame, "Sei sicuro di voler uscire?",
                    "Logout", YES_NO_OPTION, QUESTION_MESSAGE, null, options, options[0]);
            // Se la scelta è stata positiva, faccio il logout
            if (choice == YES_OPTION) if (frame.logoutClient()) {
                clearAll();
                setSelectedTab(0);
            }
        }
    }
}