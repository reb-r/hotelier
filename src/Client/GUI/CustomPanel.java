package Client.GUI;

import javax.swing.*;


/**
 * <p align="justify">
 *     La classe astratta {@code CustomPanel} estende la classe {@link JPanel} e costituisce la base per le pagine
 *     dell'interfaccia.
 * </p>
 */
public abstract class CustomPanel extends JPanel {
    /** Parent frame */
    final CustomFrame frame;
    /** Insieme di tabs tra cui l'utente può cambiare */
    final JTabbedPane tabs = new JTabbedPane(SwingConstants.LEFT);
    /** Tab di ricerca */
    final SearchTab search_tab;

    public CustomPanel(CustomFrame frame) {
        // Assegno i parametri e inizializzo le variabili
        this.frame = frame;
        search_tab = new SearchTab(frame);
    }


    /**
     * <p align="justify">
     *     Inizializza tutte le componenti presenti nel panel.
     * </p>
     */
    abstract void clearAll();


    // Metodi get e set
    int getSelectedTab() { return tabs.getSelectedIndex(); }

    void setSelectedTab(int index) {
        if (index == 1) tabs.setEnabledAt(index, true);
        tabs.setSelectedIndex(index);
    }


    // Altri metodi
    /**
     * Visualizza i risultati della ricerca.
     * @param results gli elementi da visualizzare
     */
    void displaySearchResults(String[] results) {
        setSelectedTab(1);
        search_tab.displayResults(results);
    }
}