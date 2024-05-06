package Client.GUI;

import Server.Database.Hotel.Ratings;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import static Client.GUI.CustomFrame.*;
import static javax.swing.JOptionPane.*;


/**
 * <p align="justify">
 *     La classe {@code ReviewPostDialog} estende la classe {@link JDialog} ed è la finestra dedicata per l'assegnazione
 *     dei punteggi della recensione da pubblicare.
 * </p>
 */
public class ReviewPostDialog extends JDialog {
    static final NumberFormat format = DecimalFormat.getInstance();

    /** Parent frame */
    final CustomFrame frame;
    /** Campo per l'hotel */
    private final JTextField hotel_field = new JTextField();
    /** Campo per la città */
    private final JTextField city_field = new JTextField();
    /** Slider per il rate */
    private final JSlider rate_slider = new JSlider(0, 50, 0);
    /** Campo formattato per il rate */
    private final JFormattedTextField rate_field;
    /** Insieme di sliders per i ratings */
    private final JSlider[] slider = new JSlider[4];
    /** Insieme di campi per i ratings */
    private final JFormattedTextField[] formatted_field = new JFormattedTextField[4];
    /** Pulsante di pubblicazione */
    private final JButton post_button = new JButton("Pubblica");
    /** Pulsante di annullamento */
    private final JButton cancel_button = new JButton("Annulla");

    public ReviewPostDialog(CustomFrame parent, boolean modal, String hotel, String city) {
        // Chiamo il costruttore della superclasse, assegno i parametri e inizializzo le variabili
        super(parent, "Insert review", modal);
        frame = parent;

        format.setMinimumIntegerDigits(1);
        format.setMaximumIntegerDigits(1);
        format.setMaximumFractionDigits(1);
        rate_field = new JFormattedTextField(format);

        // Imposto le preferenze delle variabili e gli elementi aggiuntivi
        setLayout(new BorderLayout(0, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        rate_field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "0");
        hotel_field.setText(hotel);
        city_field.setText(city);
        rate_field.setToolTipText("Il valore inserito deve essere compreso tra 0 e 5.\n" +
                "Sono ammessi numeri decimali con una cifra dopo la virgola.");
        hotel_field.setEnabled(false);
        city_field.setEnabled(false);
        post_button.setEnabled(false);
        defineComponentsBehavior();

        // Definisco un panel contenitore per alcune componenti e imposto le preferenze
        JPanel container = new JPanel(new BorderLayout(0, 20));
        container.setPreferredSize(new Dimension(280, 450));
        // Definisco il panel dedicato alle informazioni sull'hotel e vi aggiungo i componenti
        JPanel hotel_panel = new JPanel(new GridLayout(0, 1));
        hotel_panel.setPreferredSize(new Dimension(container.getWidth(), 120));
        hotel_panel.add(new JLabel("Hotel:", JLabel.LEFT)).setFont(large_font);
        hotel_panel.add(hotel_field).setFont(large_font);
        hotel_panel.add(new JLabel("Città:", JLabel.LEFT)).setFont(large_font);
        hotel_panel.add(city_field).setFont(large_font);

        // Definisco il panel dedicato all'assegnazione dei punteggi e imposto le preferenze
        JPanel container_panel = new JPanel(new BorderLayout(0, 10));
        container_panel.setPreferredSize(new Dimension(container.getWidth(), 330));

        // Definisco il panel dedicato al rate
        JPanel rate_panel = new JPanel(new GridLayout(0, 1));
        rate_panel.setPreferredSize(new Dimension(container.getWidth(), 60));
        // Definisco un panel ausiliario per gestire le componenti per l'attribuzione del punteggio
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.add(rate_slider);
        panel.add(rate_field);
        // Aggiungo i componenti al panel per il rate
        rate_panel.add(new JLabel("Punteggio complessivo", JLabel.LEFT)).setFont(large_font);
        rate_panel.add(panel);

        // Definisco il panel dedicato ai ratings
        JPanel ratings_panel = new JPanel(new GridLayout(0, 1));
        ratings_panel.setPreferredSize(new Dimension(container.getWidth(), 270));
        // Per ogni punteggio definisco le componenti necessarie e aggiungo il panel ausiliario che le contiene al
        // panel generico
        for (int i = 0; i < Ratings.getnCategories(); i++) {
            // Determino che rating è
            switch (i) {
                case 0: ratings_panel.add(new JLabel("Posizione", JLabel.LEFT)).setFont(large_font);
                    break;
                case 1: ratings_panel.add(new JLabel("Pulizia", JLabel.LEFT)).setFont(large_font);
                    break;
                case 2: ratings_panel.add(new JLabel("Servizio", JLabel.LEFT)).setFont(large_font);
                    break;
                case 3: ratings_panel.add(new JLabel("Qualità-prezzo", JLabel.LEFT)).setFont(large_font);
                    break;
                default: break;
            }
            // Inizializzo le variabili, imposto le preferenze e gli elementi aggiuntivi
            slider[i] = new JSlider(0, 50, 0);
            formatted_field[i] = new JFormattedTextField(format);
            slider[i].addChangeListener(new SliderChangeListener(formatted_field[i]));
            formatted_field[i].putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "0");
            formatted_field[i].setToolTipText("Il valore inserito deve essere compreso tra 0 e 5.\n" +
                    "Sono ammessi numeri decimali con una cifra dopo la virgola.");
            formatted_field[i].setInputVerifier(new CustomInputVerifier());
            formatted_field[i].addFocusListener(new FormattedFieldFocusListener(slider[i]));
            formatted_field[i].addCaretListener(new FormattedFieldCaretListener());
            // Definisco un panel ausiliario dedicato al rating per gestire le componenti per l'attribuzione del punteggio
            JPanel rating_panel = new JPanel();
            rating_panel.setLayout(new BoxLayout(rating_panel, BoxLayout.LINE_AXIS));
            // Aggiungo i componenti al panel per il rating
            rating_panel.add(slider[i]);
            rating_panel.add(formatted_field[i]);
            // Aggiungo il panel per il rating al panel dei ratings
            ratings_panel.add(rating_panel);
        }
        // Aggiungo i componenti relative ai punteggi al panel contenitore
        container_panel.add(rate_panel, BorderLayout.PAGE_START);
        container_panel.add(ratings_panel);
        // Aggiungo tutto al container
        container.add(hotel_panel, BorderLayout.PAGE_START);
        container.add(container_panel);

        // Definisco un panel dedicato ai pulsanti e vi aggiungo i pulsanti
        JPanel button_panel = new JPanel(new GridLayout(1, 2));
        button_panel.setPreferredSize(new Dimension(container.getWidth(), 35));
        button_panel.add(post_button).setFont(large_font);
        button_panel.add(cancel_button).setFont(large_font);

        // Definisco i componenti da aggiungere
        JLabel title_label = new JLabel("<html>Scrivi una recensione</html>", JLabel.LEFT);
        title_label.setFont(h1_regular_font);
        title_label.setPreferredSize(new Dimension(title_label.getWidth(), 50));

        // Aggiungo tutto al panel principale di base
        add(title_label, BorderLayout.PAGE_START);
        add(container, BorderLayout.CENTER);
        add(button_panel, BorderLayout.PAGE_END);

        // Imposto le proprietà del frame
        pack();
        setResizable(false);
        setLocationRelativeTo(frame);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().requestFocusInWindow();
        setVisible(true);
    }

    private void defineComponentsBehavior() {
        rate_field.setInputVerifier(new CustomInputVerifier());
        rate_field.addFocusListener(new FormattedFieldFocusListener(rate_slider));
        rate_slider.addChangeListener(new SliderChangeListener(rate_field));
        post_button.addActionListener(new PostActionListener());
        cancel_button.addActionListener(new CancelActionListener());
    }


    // Elementi per la gestione dei componenti durante l'interazione
    /**
     * {@link InputVerifier} per i campi per il punteggio
     * Convalida l'input inserito.
     */
    static class CustomInputVerifier extends InputVerifier {
        @Override
        public boolean verify(JComponent input) {
            // Ottengo il contenuto del campo
            String text = ((JFormattedTextField) input).getText().replace(",", ".");
            // Controllo se l'input contiene spazi e lo resetto a vuoto
            if (text.isBlank()) ((JFormattedTextField) input).setText("");
            else {
                // Provo a effettuare il parsing del contenuto
                double score;
                try {
                    score = Double.parseDouble(text);
                } catch (NumberFormatException ignored) {
                    // Nel caso in cui non sia stato inserito un numero, l'input non è valido e lo comunico
                    showMessageDialog(input.getRootPane(), "L'input deve essere un valore numerico compreso tra 0 e 5.",
                            "Input warning", WARNING_MESSAGE);
                    return false;
                }
                // Altrimenti se non rispetta le condizioni forzo il valore e lo comunico
                if (score < 0) ((JFormattedTextField) input).setValue(0);
                if (score > 5) ((JFormattedTextField) input).setValue(5);
                else return true;
                showMessageDialog(input.getRootPane(), "Il valore inserito deve essere compreso tra 0 e 5.",
                        "Input warning", WARNING_MESSAGE);
            }
            return true;
        }
    }

    /**
     * <p align="justify">
     *     {@link ChangeListener} per lo slider corrispondente a un campo per il punteggio
     * </p> <p align="justify">
     *     Gestisce il comportamento del componente corrispondente.
     *  </p>
     */
    class SliderChangeListener implements ChangeListener {

        /** Campo formattato per il punteggio */
        private final JFormattedTextField field;

        SliderChangeListener(JFormattedTextField field) { this.field = field; }
        @Override
        public void stateChanged(ChangeEvent e) {
            // Imposto il contenuto del campo corrispondente con il valore che rappresenta la posizione dello slider
            field.setValue((double) ((JSlider) e.getSource()).getValue()/10.0);
        }

    }
    /**
     * <p align="justify">
     *     {@link FocusListener} per il campo corrispondente a uno slider
     * </p> <p align="justify">
     *     Gestisce il comportamento del componente corrispondente.
     *  </p>
     */
    class FormattedFieldFocusListener implements FocusListener {

        /** Slider per il punteggio */
        private final JSlider slider;

        FormattedFieldFocusListener(JSlider slider) { this.slider = slider; }

        @Override
        public void focusGained(FocusEvent e) { }
        @Override
        public void focusLost(FocusEvent e) {
            // Ottengo il contenuto del campo
            String text = ((JFormattedTextField) e.getSource()).getText().replace(",", ".");
            // Se il campo è vuoto, imposto lo slider a 0
            if (text.isBlank()) slider.setValue(0);
            // Altrimenti imposto lo slider con il valore corrispondente inserito nel campo
            else {
                try {
                    slider.setValue((int) Math.round(Double.parseDouble(text) * 10));
                } catch (NumberFormatException ignored) { }
            }
            // Controllo se ad aver ottenuto il focus è stato il pulsante di pubblicazione e "confermo" l'azione
            if (e.getOppositeComponent() != null && e.getOppositeComponent().equals(post_button)) post_button.doClick();
        }

    }

    /**
     * <p align="justify">
     *     {@link CaretListener} per i campi per i punteggi
     * </p> <p align="justify">
     *     Gestisce il comportamento del componente corrispondente.
     *  </p>
     */
    class FormattedFieldCaretListener implements CaretListener {
        @Override
        public void caretUpdate(CaretEvent e) {
            // Controllo che il campo per il rate non sia vuoto altrimenti disabilito il pulsante di pubblicazione
            if (!rate_field.getText().isBlank()) {
                // Controllo che anche gli altri campi per i ratings non lo siano e in quel caso abilito il pulsante
                for (JFormattedTextField formatted_field: formatted_field) if (formatted_field.getText().isBlank()) {
                        post_button.setEnabled(false);
                        return;
                }
                post_button.setEnabled(true);
            } else post_button.setEnabled(false);
        }

    }

    /**
     * {@link ActionListener} per il pulsante di pubblicazione della recensione
     */
    class PostActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Ottengo i valori inseriti nei campi
            double[] scores = new double[formatted_field.length];
            for (int i = 0; i < formatted_field.length; i++) scores[i] = Double.parseDouble(formatted_field[i].getValue().toString());
            // Pubblico la recensione
            frame.insertReviewClient(hotel_field.getText(), city_field.getText(), (double) rate_field.getValue(), scores);
            ((JComponent) e.getSource()).getParent().setVisible(false);
            dispose();
        }
    }

    /**
     * @see NotificationDialog.CancelActionListener
     */
    class CancelActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            ((JComponent) e.getSource()).getParent().setVisible(false);
            dispose();
        }
    }
}