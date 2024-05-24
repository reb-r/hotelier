package Client.GUI;

import Server.Database.Hotel.City;
import Server.Database.Hotel.Hotel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

import static Client.GUI.CustomFrame.*;


/**
 * <p align="justify">
 *     La classe {@code HomeTab} estende la classe {@link CustomTab} ed è la tab dedicata alla home.
 * </p>
 */
public class HomeTab extends CustomTab {
    /** Lista completa degli hotel disponibili */
    static String[] hotelList;
    /** Campo di ricerca per l'hotel */
    private final JTextField search_field = new JTextField();
    /** Campo di ricerca per la scelta della città */
    private final JComboBox<String> search_combobox = new JComboBox<>();
    /** Campo per la scelta dell'hotel per la recensione */
    private final JComboBox<String> hotel_combobox = new JComboBox<>();
    /** Campo per la scelta della città per la recensione */
    private final JComboBox<String> city_combobox = new JComboBox<>();
    /** Pulsante di ricerca */
    private final JButton search_button = new JButton("Cerca");
    /** Pulsante per la pubblicazione della recensione */
    private final JButton review_button = new JButton("Pubblica recensione");

    public HomeTab(CustomFrame parent, Dimension dimension) throws IOException, FontFormatException {
        // Chiamo il costruttore della superclasse, assegno i parametri e inizializzo le variabili
        super(parent);
        hotelList = frame.getHotelValues();
        for (City city: City.values()) {
            search_combobox.addItem(city.getName());
            city_combobox.addItem(city.getName());
        }

        // Imposto le preferenze delle variabili e gli elementi aggiuntivi
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(dimension.width - 120, dimension.height));
        setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 10));
        search_field.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSearchIcon());
        search_field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nome hotel");
        search_combobox.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Città");
        hotel_combobox.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Hotel");
        city_combobox.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Città");
        AutoCompleteDecorator.decorate(search_combobox);
        AutoCompleteDecorator.decorate(hotel_combobox);
        AutoCompleteDecorator.decorate(city_combobox);
        defineComponentsBehavior();

        // Definisco i componenti nel panel
        // Definisco il panel dedicato alla scritta iniziale
        JPanel title_panel = new JPanel(new BorderLayout());
        // Definisco i componenti da aggiungere
        JLabel title_label = new JLabel("<html>Trova l'hotel ideale per il tuo soggiorno da sogno...</html>", JLabel.LEFT);
        title_label.setFont(Font.createFont(Font.TRUETYPE_FONT, new File(path + "fonts/seguisb.ttf")).deriveFont(36f));
        // Aggiungo i componenti
        title_panel.add(Box.createVerticalStrut(30), BorderLayout.PAGE_START);
        title_panel.add(title_label, BorderLayout.CENTER);
        title_panel.add(Box.createVerticalStrut(50), BorderLayout.PAGE_END);

        // Definisco il panel dedicato alla ricerca e imposto le preferenze
        JPanel search_panel = new JPanel(new GridLayout(0, 1, 0, 10));
        search_panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 15));
        search_panel.setPreferredSize(new Dimension(400, 300));
        // Aggiungo i componenti
        search_panel.add(new JLabel("Cerca hotel", JLabel.LEFT)).setFont(h0_font);
        search_panel.add(search_field).setFont(h2_regular_font);
        search_panel.add(new JLabel("a", JLabel.LEFT)).setFont(h0_font);
        search_panel.add(search_combobox).setFont(h2_regular_font);
        search_panel.add(search_button).setFont(large_font);

        // Definisco il panel dedicato alla recensione
        JPanel right_panel = new JPanel(new BorderLayout(0, 10));
        // Imposto le preferenze
        right_panel.setPreferredSize(new Dimension(250, right_panel.getHeight()));
        right_panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 20, 0));
        // Definisco il panel dedicato alla scritta
        JPanel review_panel = new JPanel(new BorderLayout(0, 5));
        // Definisco i componenti da aggiungere
        JLabel review_label = new JLabel("Scrivi una recensione", JLabel.LEFT);
        JLabel label = new JLabel("<html>Consiglia il tuo hotel preferito anche agli altri utenti di HOTELIER.</html>");
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        review_label.setFont(h1_font);
        // Aggiungo i componenti al panel minore
        review_panel.add(Box.createVerticalStrut(10), BorderLayout.PAGE_START);
        review_panel.add(review_label, BorderLayout.CENTER);
        review_panel.add(label, BorderLayout.PAGE_END);
        // Definisco il panel dedicato ai campi e vi aggiungo i componenti
        JPanel fields_panel = new JPanel(new GridLayout(0, 1));
        fields_panel.add(new JLabel("Hotel:", JLabel.LEFT)).setFont(h3_regular_font);
        fields_panel.add(hotel_combobox).setFont(h3_regular_font);
        fields_panel.add(new JLabel("Città:", JLabel.LEFT)).setFont(h3_regular_font);
        fields_panel.add(city_combobox).setFont(h3_regular_font);
        fields_panel.add(review_button).setFont(large_font);
        // Aggiungo i componenti
        right_panel.add(review_panel, BorderLayout.PAGE_START);
        right_panel.add(fields_panel);

        // Aggiungo tutto al panel principale di base
        add(title_panel, BorderLayout.PAGE_START);
        add(search_panel, BorderLayout.LINE_START);
        add(new JSeparator(SwingConstants.VERTICAL), BorderLayout.CENTER);
        add(right_panel, BorderLayout.LINE_END);

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
        hotel_combobox.addItemListener(new HotelItemListener());
        hotel_combobox.getEditor().getEditorComponent().addKeyListener(new PerformAction(review_button));
        city_combobox.addItemListener(new CityItemListener());
        city_combobox.getEditor().getEditorComponent().addFocusListener(new CityFocusAdapter());
        city_combobox.getEditor().getEditorComponent().addKeyListener(new PerformAction(review_button));
        search_button.addActionListener(new SearchActionListener());
        review_button.addActionListener(new ReviewActionListener());
    }


    @Override
    void clearAll() {
        search_field.setText("");
        search_combobox.setSelectedIndex(-1);
        search_button.setEnabled(false);
        clear();
    }


    /**
     * <p align="justify">
     *     Pulisce il panel dedicato alla recensione, impostando ai valori di default le componenti.
     * </p>
     */
    private void clear() {
        hotel_combobox.removeAllItems();
        for (String hotel: hotelList) hotel_combobox.addItem(hotel);
        hotel_combobox.setSelectedIndex(-1);
        city_combobox.setSelectedIndex(-1);
        review_button.setEnabled(false);
        frame.requestFocusInWindow();
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
     *     {@link FocusAdapter} per il campo relativo alle città per la recensione
     * </p> <p align="justify">
     *     Gestisce la visualizzazione del menu a tendina per la scelta della città quando l'elemento acquisisce il focus.
     * </p>
     */
    class CityFocusAdapter extends FocusAdapter {
        @Override
        public void focusGained(FocusEvent e) {
            city_combobox.showPopup();
        }
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
     * <p align="justify">
     *     {@link ItemListener} per il campo relativo agli hotel per la recensione
     * </p> <p align="justify">
     *     Gestisce la corretta visualizzazione degli elementi selezionabili.
     * </p>
     */
    class HotelItemListener implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent e) {
            // Controllo che ci sia stato un cambiamento nella selezione e che sia stato selezionato un hotel
            if (e.getStateChange() == ItemEvent.SELECTED) if (hotel_combobox.getSelectedIndex() != -1)
                // Forzo la scelta sulla selezione della città solamente nel caso in cui:
                // - la scelta sugli hotel non sia stata ancora ridotta
                // - la città selezionata non sia la stessa già precedentemente selezionata
                if (hotel_combobox.getItemCount() > Hotel.Type.N_TYPES &&
                        city_combobox.getSelectedIndex() != hotel_combobox.getSelectedIndex() / Hotel.Type.N_TYPES) {
                    // Disabilito momentaneamente il campo per la città per evitare un'ulteriore errata modifica
                    city_combobox.setEnabled(false);
                    city_combobox.setSelectedIndex(hotel_combobox.getSelectedIndex() / Hotel.Type.N_TYPES);
                }
            // Abilito il pulsante corrispondente solo nel caso in cui per entrambi i campi per la recensione sia stato
            // selezionato un elemento
            review_button.setEnabled(hotel_combobox.getSelectedIndex() != -1 && city_combobox.getSelectedIndex() != -1);
        }
    }

    /**
     * <p align="justify">
     *     {@link ItemListener} per il campo relativo alle città per la recensione
     * </p> <p align="justify">
     *     Gestisce la corretta visualizzazione degli elementi selezionabili.
     * </p>
     */
    class CityItemListener implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent e) {
            // Controllo che ci sia stato un cambiamento nella selezione e che sia stata selezionata una città
            if (e.getStateChange() == ItemEvent.SELECTED) if (city_combobox.getSelectedIndex() != -1) {
                // Se il campo è abilitato significa che posso apportare delle modifiche ai campi per la recensione,
                // così da filtrare gli hotels in base alla città selezionata, altrimenti lo riabilito
                if (city_combobox.isEnabled()) {
                    // Determino l'indice del primo hotel situato nella città selezionata (ogni città ha N_TYPES hotels)
                    int index = city_combobox.getSelectedIndex() * Hotel.Type.N_TYPES;
                    // Nascondo e rimuovo tutti gli hotels
                    hotel_combobox.setPopupVisible(false);
                    hotel_combobox.removeAllItems();
                    // Aggiungo gli hotels situati nella città selezionata
                    for (int i = index; i < index + 5; i++) hotel_combobox.addItem(hotelList[i]);
                    hotel_combobox.setSelectedIndex(-1);
                } else city_combobox.setEnabled(true);
            }
            // Abilito il pulsante corrispondente solo nel caso in cui per entrambi i campi per la recensione sia stato
            // selezionato un elemento
            review_button.setEnabled(hotel_combobox.getSelectedIndex() != -1 && city_combobox.getSelectedIndex() != -1);
        }
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
     *  @see HomePanel.SearchActionListener
     */
    class SearchActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (isBlank(search_field)) frame.searchClient(null, search_combobox.getSelectedItem().toString());
            else frame.searchClient(search_field.getText(), search_combobox.getSelectedItem().toString());
            clearAll();
        }
    }

    /**
     *  <p align="justify">
     *      {@link ActionListener} per il pulsante per la pubblicazione della recensione
     *  </p> <p align="justify">
     *     Gestisce il comportamento del pulsante corrispondente.
     *  </p>
     */
    class ReviewActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Determino hotel e città della recensione a cui questa fa riferimento
            String hotel = hotel_combobox.getSelectedItem().toString();
            String city = city_combobox.getSelectedItem().toString();
            clear();
            // Mostro la finestra per l'assegnazione dei punteggi e pulisco il panel dedicato alla recensione
            new ReviewPostDialog(frame, true, hotel, city);
        }
    }
}