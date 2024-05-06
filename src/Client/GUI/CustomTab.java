package Client.GUI;

import javax.swing.*;


/**
 * <p align="justify">
 *     La classe astratta {@code CustomTab} estende la classe {@link JPanel} e rappresenta un generico panel che
 *     idealmente può essere contenuto in un {@code JTabbedPane}.
 * </p>
 */
public abstract class CustomTab extends JPanel {
    /** Parent frame */
    final CustomFrame frame;

    public CustomTab(CustomFrame frame) { this.frame = frame; }


    /**
     * <p align="justify">
     *     Pulisce la tab, impostando ai valori di default le componenti.
     * </p>
     */
    abstract void clearAll();
}