package Client.GUI;

import Server.Database.Hotel.City;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.List;

import static Client.GUI.CustomFrame.*;


/**
 * <p align="justify">
 *     La classe {@code NotificationDialog} estende la classe {@link JDialog} ed è la finestra dedicata alla scelta
 *     delle città di interesse per la ricezione delle notifiche di aggiornamento classifica.
 * </p>
 */
public class NotificationDialog extends JDialog {
    /** Parent frame */
    final CustomFrame frame;
    /** Pulsante di conferma */
    private final JButton confirm_button = new JButton("OK");
    /** Pulsante di annullamento */
    private final JButton cancel_button = new JButton("Annulla");
    /** Lista di caselle per il check corrispondenti alle città selezionabili */
    private final LinkedList<JCheckBox> checkBoxes = new LinkedList<>();

    public NotificationDialog(CustomFrame parent, Boolean modal) {
        // Chiamo il costruttore della superclasse, assegno i parametri e inizializzo le variabili
        super(parent, "Notification event", modal);
        frame = parent;

        // Imposto le preferenze delle variabili e gli elementi aggiuntivi
        setLayout(new BorderLayout(0, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 20, 15, 20));
        confirm_button.setFont(UIManager.getFont("h4.font"));
        confirm_button.setForeground(Color.WHITE);
        confirm_button.setBackground(BLUE_COLOR);
        defineComponentsBehavior();

        // Definisco un panel contenitore per alcune componenti e imposto le preferenze
        JPanel container = new JPanel(new GridLayout(10, 2));
        container.setPreferredSize(new Dimension(300, 320));
        container.setBorder(new CompoundBorder(BorderFactory.createTitledBorder("Città disponibili"),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        // Aggiungo i componenti al container inizializzandoli
        for (City city: City.values()) {
            JCheckBox checkBox = new JCheckBox(city.getName());
            checkBox.setFont(large_font);
            checkBoxes.add(checkBox);
            container.add(checkBox);
        }

        // Definisco il panel dedicato alla scritta iniziale
        JPanel title_panel = new JPanel(new BorderLayout(0,5));
        JLabel info_label =
                new JLabel("<html>Seleziona le città di interesse per cui intendi ricevere notifiche sull'aggiornamento dei relativi ranking locali.</html>"
                        + "<html>Se desideri saltare questo passaggio, premi ANNULLA.</html>");
        info_label.setPreferredSize(new Dimension(300, 80));
        info_label.setFont(large_font);
        // Aggiungo i componenti
        title_panel.add(new JLabel("Ricevi notifiche", JLabel.LEFT)).setFont(h1_regular_font);
        title_panel.add(info_label, BorderLayout.PAGE_END);

        // Definisco un panel dedicato ai pulsanti e vi aggiungo i pulsanti
        JPanel panel = new JPanel(new BorderLayout());
        JPanel button_panel = new JPanel(new GridLayout(1, 2));
        button_panel.add(confirm_button);
        button_panel.add(cancel_button);
        panel.add(button_panel, BorderLayout.LINE_END);

        // Aggiungo tutto al panel principale di base
        add(title_panel, BorderLayout.PAGE_START);
        add(container, BorderLayout.CENTER);
        add(panel, BorderLayout.PAGE_END);

        // Imposto le proprietà del frame
        pack();
        setResizable(false);
        setLocationRelativeTo(getOwner());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        confirm_button.requestFocusInWindow();
        setVisible(true);
    }

    private void defineComponentsBehavior() {
        confirm_button.addActionListener(new ConfirmActionListener());
        cancel_button.addActionListener(new CancelActionListener());
    }


    // Elementi per la gestione dei componenti durante l'interazione
    /**
     * {@link ActionListener} per il pulsante di annullamento
     * Consente di annullare.
     */
    class ConfirmActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            List<String> cities = new LinkedList<>();
            for (JCheckBox checkBox: checkBoxes) if (checkBox.isSelected()) cities.add(checkBox.getText());
            frame.registerForCallbackClient(cities);
            ((JComponent) e.getSource()).getParent().setVisible(false);
            dispose();
        }
    }

    /**
     * {@link ActionListener} per il pulsante di annullamento
     * Consente di annullare.
     */
    class CancelActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            ((JComponent) e.getSource()).getParent().setVisible(false);
            dispose();
        }

    }
}