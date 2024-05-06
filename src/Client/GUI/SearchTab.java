package Client.GUI;

import Server.Database.Hotel.City;
import Server.Database.Hotel.Hotel;
import Server.Database.Hotel.Hotel.Type;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

import static Client.GUI.CustomFrame.*;
import static Client.GUI.CustomFrame.NamePanel.*;
import static Server.Database.Hotel.Hotel.Type.*;
import static javax.swing.JOptionPane.*;
import static javax.swing.ScrollPaneConstants.*;


/**
 * <p align="justify">
 *     La classe {@code SearchTab} estende la classe {@link CustomTab} ed è la tab dedicata alla ricerca e alla
 *     visualizzazione dei risultati ottenuti.
 * </p>
 */
public class SearchTab extends CustomTab {
    // Icone personalizzate
    static final Icon location_pin = createImageIcon(path + "icons/location-pin.png", 14);
    static final Icon telephone = createImageIcon(path + "icons/telephone.png", 13);
    static final Icon full_star = createImageIcon(path + "icons/star-full.png", 12);
    static final Icon empty_star = createImageIcon(path + "icons/star-empty.png", 12);

    /** Campo di ricerca per l'hotel */
    private final JTextField search_field = new JTextField();
    /** Campo di ricerca per la scelta della città */
    private final JComboBox<String> search_combobox = new JComboBox<>();
    /** Pulsante di ricerca */
    private final JButton search_button = new JButton("Cerca");
    /** Panel dedicato ai risultati della ricerca */
    private final JPanel results_panel = new JPanel();
    /** Componente per la gestione dello scroll */
    private final JScrollPane scrollPane = new JScrollPane(results_panel, VERTICAL_SCROLLBAR_NEVER, HORIZONTAL_SCROLLBAR_NEVER);
    /** Etichetta con icona per il refresh */
    private final JLabel refresh_label = new JLabel(createImageIcon(path + "icons/refresh.png", 28));
    /** Etichetta informativa */
    private final JLabel info_label = new JLabel("La tua ricerca non ha prodotto alcun risultato.");

    public SearchTab(CustomFrame parent) {
        // Chiamo il costruttore della superclasse
        super(parent);
        for (City city: City.values()) search_combobox.addItem(city.getName());

        // Imposto le preferenze delle variabili e gli elementi aggiuntivi
        setLayout(new BorderLayout(0, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 10));
        scrollPane.putClientProperty(FlatClientProperties.STYLE, "borderWidth:0");
        search_field.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSearchIcon());
        search_field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Cerca hotel");
        search_combobox.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Città");
        refresh_label.setToolTipText("Ricarica l'ultima ricerca effettuata");
        refresh_label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        search_button.setFont(UIManager.getFont("h3.regular.font"));
        scrollPane.getVerticalScrollBar().setUnitIncrement(15);
        results_panel.setLayout(new BoxLayout(results_panel, BoxLayout.PAGE_AXIS));
        results_panel.setBorder(BorderFactory.createEmptyBorder(15, 10, 0, 10));
        results_panel.setBackground(GRAY_COLOR);
        search_field.setPreferredSize(new Dimension(230, search_field.getHeight()));
        search_field.setFont(h3_regular_font);
        search_combobox.setPreferredSize(new Dimension(180, search_field.getHeight()));
        AutoCompleteDecorator.decorate(search_combobox);
        defineComponentsBehavior();

        // Definisco il panel di ricerca, che contiene i campi necessari per la ricerca
        JPanel search_panel = new JPanel();
        // Imposto le preferenze
        search_panel.setLayout(new BoxLayout(search_panel, BoxLayout.LINE_AXIS));
        search_panel.setPreferredSize(new Dimension(getWidth(), 32));
        // Aggiungo i componenti
        search_panel.add(refresh_label);
        search_panel.add(search_field);
        search_panel.add(new JLabel("  a ")).setFont(h2_regular_font);
        search_panel.add(search_combobox).setFont(h3_regular_font);
        search_panel.add(search_button).setPreferredSize(new Dimension(90, search_panel.getHeight()));
        search_panel.add(Box.createHorizontalStrut(11));

        // Aggiungo tutto al panel principale di base
        add(search_panel, BorderLayout.PAGE_START);
        add(scrollPane, BorderLayout.CENTER);
        add(scrollPane.getVerticalScrollBar(), BorderLayout.LINE_END);

        // Inizializzo i componenti
        clearAll();
    }

    private void defineComponentsBehavior() {
        search_field.addCaretListener(new SearchCaretListener());
        search_field.addKeyListener(new PerformAction(search_button));
        search_combobox.setRenderer(new CityComboBoxRender());
        search_combobox.addItemListener(new SearchItemListener());
        search_combobox.getEditor().getEditorComponent().addFocusListener(new SearchFocusAdapter());
        search_combobox.getEditor().getEditorComponent().addKeyListener(new PerformAction(search_button));
        refresh_label.addMouseListener(new RefreshMouseAdapter());
        search_button.addActionListener(new SearchActionListener());
    }


    @Override
    void clearAll() {
        search_field.setText("");
        search_combobox.setSelectedIndex(-1);
        search_button.setEnabled(false);
        if (info_label.getParent() == this) scrollPane.remove(info_label);
        else for (Component component: results_panel.getComponents()) results_panel.remove(component);
        frame.requestFocusInWindow();
    }


    /**
     * <p align="justify">
     * Mostra gli hotels individuati indicati nella tab.
     * </p>
     *
     * @param results la lista di risultati ottenuti dalla ricerca da visualizzare
     */
    void displayResults(String[] results) {
        // Prima di visualizzare i risultati pulisco il panel dedicato
        clearAll();
        if (results == null) return;
        // Aggiungo ogni hotel al panel dedicato, se ce n'è uno solo gestisco lo spazio restante
        if (results.length > 1) for (String result: results) addResult(result);
        else if (results.length == 1) {
            results_panel.add(Box.createVerticalStrut(473 - addResult(results[0]).getHeight()));
            results_panel.add(Box.createVerticalGlue());
        } else results_panel.add(info_label);
    }

    private JPanel addResult(String result) {
        // Identifico i singoli campi suddividendo la stringa
        String[] element = result.replaceAll("[{|}]", "").split(";");

        // Definisco il panel dedicato all'hotel e imposto le preferenze
        JPanel result_panel = new JPanel();
        result_panel.setLayout(new BoxLayout(result_panel, BoxLayout.LINE_AXIS));
        result_panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        result_panel.setAlignmentX(LEFT_ALIGNMENT);
        result_panel.putClientProperty(FlatClientProperties.STYLE, "arc:20");

        // Definisco il panel dedicato alle informazioni generali e imposto le preferenze
        JPanel info_panel = new JPanel();
        info_panel.setLayout(new BoxLayout(info_panel, BoxLayout.PAGE_AXIS));
        info_panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        info_panel.setPreferredSize(new Dimension(300, 232));
        info_panel.setAlignmentX(LEFT_ALIGNMENT);
        // Definisco il panel dedicato ai servizi offerti e imposto le preferenze
        JPanel services_panel = new JPanel();
        services_panel.setLayout(new BoxLayout(services_panel, BoxLayout.PAGE_AXIS));
        services_panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 20));
        services_panel.setAlignmentX(LEFT_ALIGNMENT);

        // Aggiungo i due panel al panel dell'hotel
        result_panel.add(info_panel);
        result_panel.add(services_panel);

        // Definisco un panel dedicato ai pulsanti
        JPanel buttons_panel = null;

        for (int field = 0; field < Hotel.getnFields(); field++) {
            switch (field) {
                // NOME
                case 0:
                    JLabel hotel_label = new JLabel(element[field].trim(), JLabel.LEFT);
                    info_panel.add(hotel_label).setFont(UIManager.getFont("h3.font"));
                    info_panel.add(new JLabel(element[5].trim()));
                    info_panel.add(Box.createVerticalStrut(3));
                    break;
                // INDIRIZZO
                case 1:
                    JLabel address_label =
                            new JLabel(element[field].trim() + ", " + element[2].trim(), location_pin, JLabel.LEFT);
                    info_panel.add(address_label).setFont(large_font);
                    break;
                // CITTÀ
                case 2:
                    buttons_panel = buttonPanel(element[0].trim(), element[field].trim());
                    break;
                // TELEFONO
                case 4:
                    JLabel phone_label = new JLabel(element[field].trim(), telephone, JLabel.LEFT);
                    info_panel.add(phone_label).setFont(large_font);
                    info_panel.add(Box.createVerticalStrut(5));
                    JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
                    separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                    info_panel.add(separator);
                    info_panel.add(Box.createVerticalStrut(1));
                    break;
                // DESCRIZIONE -> case 5
                // SERVIZI
                case 6:
                    services_panel.add(new JLabel("Servizi offerti dalla struttura")).setFont(large_font);
                    if (!element[field].replaceAll("[\\[\\]]", "").isBlank())
                        Arrays.stream(element[field].replaceAll("[\\[\\]]", "").split(","))
                                .forEach(feature -> services_panel.add(new JLabel("> " + feature.trim())));
                    services_panel.add(Box.createVerticalStrut(10));
                    services_panel.add(new JLabel("Categoria dell'hotel")).setFont(large_font);
                    services_panel.add(addType(element[3].trim()));
                    services_panel.add(Box.createVerticalGlue());
                    break;
                // RATE
                case 7:
                    info_panel.add(new JLabel("Punteggio complessivo")).setFont(large_font);
                    info_panel.add(addScore(element[field].trim(), "", true));
                    info_panel.add(new JLabel("N." + element[9].trim() + " su " +
                            Hotel.Type.N_TYPES + " hotel a " + element[2].trim()));
                    info_panel.add(Box.createVerticalStrut(3));
                    break;
                // RATINGS
                case 8:
                    info_panel.add(new JLabel("Punteggi per categoria")).setFont(large_font);
                    displayRatings(info_panel, element[field].replaceAll("[\\[|\\]]", "").split(","));
                    info_panel.add(Box.createVerticalGlue());
                    break;
                default:
                    break;
            }
        }

        // Aggiungo il panel dell'hotel al panel di tutti gli hotels
        results_panel.add(result_panel);
        // Aggiungo il panel dedicato ai pulsanti
        results_panel.add(Box.createVerticalStrut(1));
        results_panel.add(buttons_panel);
        results_panel.add(Box.createVerticalStrut(15));
        frame.pack();
        return result_panel;
    }

    /**
     * <p align="justify">
     * Crea un panel dedicato in cui sono presenti rispettivamente un pulsante dedicato alla visualizzazione di
     * tutte le recensioni riferite all'hotel indicato e uno dedicato alla pubblicazione di una recensione riferita
     * sempre all'hotel indicato.
     * </p>
     *
     * @param hotel l'hotel a cui fanno riferimento i pulsanti
     * @param city  la città in cui è situato l'hotel indicato
     * @return un panel dedicato ai pulsanti per mostrare tutte le recensioni o per pubblicare una recensione
     */
    private JPanel buttonPanel(String hotel, String city) {
        // Definisco il pulsante per mostrare le recensioni e imposto le preferenze
        JButton reviews_button = new JButton("Mostra recensioni");
        reviews_button.setPreferredSize(new Dimension(reviews_button.getWidth(), 35));
        reviews_button.putClientProperty(FlatClientProperties.STYLE, "arc:20");
        reviews_button.addActionListener(new ShowReviewsMouseListener(hotel, city));
        // Definisco il pulsante per pubblicare una recensione e imposto le preferenze
        JButton review_button = new JButton("Scrivi una recensione");
        review_button.setPreferredSize(new Dimension(review_button.getWidth(), 35));
        review_button.putClientProperty(FlatClientProperties.STYLE, "arc:20");
        review_button.addActionListener(new AddReviewMouseListener(hotel, city));

        // Definisco il panel dedicato contenente i due pulsanti e imposto le preferenze
        JPanel button_panel = new JPanel(new GridLayout(1, 2));
        button_panel.setOpaque(false);
        button_panel.setAlignmentX(LEFT_ALIGNMENT);
        // Aggiungo i componenti
        button_panel.add(reviews_button);
        button_panel.add(review_button);
        return button_panel;
    }

    /**
     * <p align="justify">
     * Visualizza graficamente il tipo indicato mediante icone.
     * </p>
     *
     * @param hotelType il tipo dell'hotel da visualizzare
     * @return il panel contenente il tipo visualizzato graficamente
     */
    private JPanel addType(String hotelType) {
        // Definisco il panel che visualizzerà graficamente il tipo e imposto le preferenze
        JPanel stars_panel = new JPanel();
        stars_panel.setLayout(new BoxLayout(stars_panel, BoxLayout.LINE_AXIS));
        stars_panel.setAlignmentX(LEFT_ALIGNMENT);
        stars_panel.setToolTipText(hotelType);
        Type type = parseType(hotelType);

        if (type == null) return stars_panel;
        else addStars(stars_panel, type.getStars(), 0);
        if (type == STARS_4S || type == STARS_5S) stars_panel.add(new JLabel("S")).setFont(large_font);
        else stars_panel.add(new JLabel(" ")).setFont(large_font);

        /* switch (type) {
            case STARS_3:
                for (int i = 0; i < 3; i++) stars_panel.add(new JLabel(full_star));
                for (int i = 0; i < 2; i++) stars_panel.add(new JLabel(empty_star));
                stars_panel.add(new JLabel(" ")).setFont(large_font);
                break;
            case STARS_4:
                for (int i = 0; i < 4; i++) stars_panel.add(new JLabel(full_star));
                stars_panel.add(new JLabel(empty_star));
                stars_panel.add(new JLabel(" ")).setFont(large_font);
                break;
            case STARS_4S:
                for (int i = 0; i < 4; i++) stars_panel.add(new JLabel(full_star));
                stars_panel.add(new JLabel(empty_star));
                stars_panel.add(new JLabel("S")).setFont(large_font);
                break;
            case STARS_5:
                for (int i = 0; i < 5; i++) stars_panel.add(new JLabel(full_star));
                stars_panel.add(new JLabel(" ")).setFont(large_font);
                break;
            case STARS_5S:
                for (int i = 0; i < 5; i++) stars_panel.add(new JLabel(full_star));
                stars_panel.add(new JLabel("S")).setFont(large_font);
                break;
        } */
        return stars_panel;
    }

    /**
     * <p align="justify">
     * Gestisce ricorsivamente l'inserimento delle icone per la visualizzazione grafica del tipo indicato.
     * </p>
     *
     * @param panel il panel contenente il tipo visualizzato graficamente
     * @param type il tipo dell'hotel
     * @param counter un contatore ausiliario, di norma inizialmente a {@code 0}
     */
    private void addStars(JPanel panel, int type, int counter) {
        if (type > counter) {
            panel.add(new JLabel(full_star));
            addStars(panel, type, ++counter);
        } else for (int i = 0; i < 5 - counter; i++) panel.add(new JLabel(empty_star));
    }


    // Elementi per la gestione dei componenti durante l'interazione
    /**
     * @see HomePanel.SearchFocusAdapter
     */
    class SearchFocusAdapter extends FocusAdapter {
        @Override
        public void focusGained(FocusEvent e) { search_combobox.showPopup(); }
    }

    /**
     * <p align="justify">
     *     {@link ItemListener} per il campo di ricerca relativo alle città
     * </p> <p align="justify">
     *     Gestisce il pulsante di ricerca in base al contenuto del campo per la scelta della città.
     * </p>
     */
    class SearchItemListener implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent e) { search_button.setEnabled(search_combobox.getSelectedIndex() != -1); }
    }

    /**
     * @see HomePanel.SearchCaretListener
     */
    class SearchCaretListener implements CaretListener {
        @Override
        public void caretUpdate(CaretEvent e) {
            search_field.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, e.getDot() > 0);
        }
    }

    /**
     * {@link MouseAdapter} per l'etichetta/icona di refresh
     * <p align="justify">
     * Gestisce il comportamento del pulsante corrispondente.
     * </p>
     */
    class RefreshMouseAdapter extends MouseAdapter {
        /*
            Quando cliccato, ricarica i risultati della ricerca appena effettuata.
         */
        @Override
        public void mouseClicked(MouseEvent e) {
            frame.searchClient(null, null);
        }
    }

    /**
     * @see HomePanel.SearchActionListener
     */
    class SearchActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (isBlank(search_combobox)) frame.searchClient(null, null);
            else if (isBlank(search_field)) frame.searchClient(null, search_combobox.getSelectedItem().toString());
            else frame.searchClient(search_field.getText(), search_combobox.getSelectedItem().toString());
        }
    }

    /**
     * <p align="justify">
     * {@link ActionListener} per il pulsante dedicato alla visualizzazione di tutte le recensioni di un hotel
     * </p> <p align="justify">
     *     Gestisce il comportamento del pulsante corrispondente.
     * </p>
     */
    class ShowReviewsMouseListener implements ActionListener {
        /** Hotel a cui fa riferimento il pulsante */
        private final String hotel;
        /** Città in cui è situato l'hotel */
        private final String city;

        public ShowReviewsMouseListener(String hotel, String city) {
            super();
            this.hotel = hotel;
            this.city = city;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Ottengo le recensioni per l'hotel
            String[] reviews = frame.showReviewsClient(hotel, city);
            // Nel caso in cui non ci siano ancora recensioni per l'hotel lo comunico con un messaggio
            if (reviews.length == 0) showMessageDialog(frame,
                    "Non è stata postata alcuna recensione al momento per questa struttura!",
                    "Show reviews info", INFORMATION_MESSAGE);
                // Altrimenti mostro la finestra dedicata con la lista di recensioni
            else new ReviewsDialog(frame, hotel, reviews);
        }
    }

    /**
     * <p align="justify">
     * {@link ActionListener} per il pulsante dedicato alla pubblicazione di una recensione riferita a un hotel
     * </p> <p align="justify">
     * Gestisce il comportamento del pulsante corrispondente.
     * </p>
     */
    class AddReviewMouseListener implements ActionListener {
        /** Hotel a cui fa riferimento il pulsante */
        private final String hotel;
        /** Città in cui è situato l'hotel */
        private final String city;

        public AddReviewMouseListener(String hotel, String city) {
            this.hotel = hotel;
            this.city = city;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Controllo se la vista è quella da guest e richiedo di effettuare l'accesso
            if (frame.getActivePanel() == GUEST_PANEL) {
                Object[] options = new Object[]{"Accedi", "Annulla"};
                int choice = showOptionDialog(frame, "Prima di continuare devi effettuare l'accesso.",
                        "Insert review", YES_NO_OPTION, QUESTION_MESSAGE, null, options, options[0]);
                if (choice == 0) new LoginDialog(frame, true);
            }
            // Se è andato a buon fine l'accesso, mostro la finestra per l'assegnazione dei punteggi
            if (frame.getActivePanel() == SESSION_PANEL) new ReviewPostDialog(frame, true, hotel, city);
            frame.requestFocusInWindow();
        }
    }
}