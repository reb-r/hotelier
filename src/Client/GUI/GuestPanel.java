package Client.GUI;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

import static Client.GUI.CustomFrame.large_font;


/**
 * <p align="justify">
 *     La classe {@code GuestPanel} estende la classe {@link CustomPanel} e rappresenta la vista dell'applicazione da guest.
 * </p>
 */
public class GuestPanel extends CustomPanel {
    /** Etichetta per il login */
    private final JLabel login_label = new JLabel("Login", JLabel.CENTER);

    public GuestPanel(CustomFrame parent, Dimension dimension) {
        // Chiamo il costruttore della superclasse
        super(parent);

        // Imposto le preferenze delle variabili e gli elementi aggiuntivi
        tabs.setFont(large_font);
        tabs.setPreferredSize(dimension);
        login_label.setFont(large_font);
        login_label.setPreferredSize(new Dimension(120, 25));
        login_label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        defineComponentsBehavior();

        // Aggiungo le tab
        tabs.add("Home", null);
        tabs.add("Cerca", search_tab);
        // Riempio lo spazio per visualizzare poi l'ultima tab
        for (int i = 0; i < 13; i++) {
            tabs.add("", null);
            tabs.setEnabledAt(tabs.getTabCount() - 1, false);
        }
        tabs.add("login", null);
        tabs.setTabComponentAt(tabs.getTabCount() - 1, login_label);

        // Aggiungo tutto al panel principale di base
        add(tabs);
    }

    private void defineComponentsBehavior() {
        tabs.addChangeListener(new TabsChangeListener());
        login_label.addMouseListener(new LoginMouseAdapter());
    }


    @Override
    void clearAll() {
        search_tab.clearAll();
        frame.requestFocusInWindow();
    }


    // Elementi per la gestione dei componenti durante l'interazione
    /**
     * {@link ChangeListener} per la selezione delle tab
     * <p align="justify">
     *     Gestisce la corretta visualizzazione delle tab selezionate.
     * </p>
     */
    class TabsChangeListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            // Se viene selezionata la tab 0 visualizzo direttamente la home page, altrimenti rimango sulla tab di ricerca
            if (tabs.getSelectedIndex() == 0) {
                frame.showHome_panel();
                clearAll();
            } else tabs.setSelectedIndex(1);
        }
    }

    /**
     * {@link MouseAdapter} per il pulsante di login
     * <p align="justify">
     *     Gestisce il comportamento del pulsante corrispondente.
     * </p>
     */
    class LoginMouseAdapter extends MouseAdapter {
        /*
            Quando cliccato, apre la finestra per il login.
         */
        @Override
        public void mouseClicked(MouseEvent e) {
            new LoginDialog(frame, true);
        }
    }
}