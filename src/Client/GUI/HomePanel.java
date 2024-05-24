package Client.GUI;

import Server.Database.Hotel.City;

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
 *     La classe {@code HomePanel} estende la classe {@link JPanel} ed è il panel dedicato alla home.
 * </p>
 */
public class HomePanel extends JPanel {
    /** Parent frame */
    final CustomFrame frame;
    /** Campo per il nome utente */
    private final JTextField username_field = new JTextField();
    /** Campo per la password */
    private final JPasswordField password_field = new JPasswordField();
    /** Pulsante di login */
    private final JButton login_button = new JButton("Accedi");
    /** Etichetta per la registrazione */
    private final JLabel register_label = new JLabel("Registrati ora.");
    /** Campo di ricerca per l'hotel */
    private final JTextField search_field = new JTextField();
    /** Campo di ricerca per la scelta della città */
    private final JComboBox<String> search_combobox = new JComboBox<>();
    /** Pulsante di ricerca */
    private final JButton search_button = new JButton("Cerca");

    public HomePanel(CustomFrame parent) throws IOException, FontFormatException {
        // Chiamo il costruttore della superclasse, assegno i parametri e inizializzo le variabili
        super(new BorderLayout());
        frame = parent;
        for (City city: City.values()) search_combobox.addItem(city.getName());

        // Imposto le preferenze delle variabili e gli elementi aggiuntivi
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        username_field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nome utente");
        password_field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Password");
        search_field.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSearchIcon());
        search_field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nome hotel");
        search_combobox.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Città");
        AutoCompleteDecorator.decorate(search_combobox);
        defineComponentsBehavior();

        // Definisco il welcome panel, che contiene la scritta di benvenuto
        JPanel welcome_panel = new JPanel();
        welcome_panel.add(new JLabel("Benvenuto su ", SwingConstants.LEFT)).setFont(UIManager.getFont("h00.font"));
        Font hotelier_font =
                Font.createFont(Font.TRUETYPE_FONT, new File(path + "fonts/segoesc.ttf")).deriveFont(40f);
        welcome_panel.add(new JLabel("Hotelier", JLabel.LEFT)).setFont(hotelier_font);
        welcome_panel.add(new JLabel("!", JLabel.LEFT)).setFont(UIManager.getFont("h00.font"));
        welcome_panel.add(Box.createVerticalStrut(180));

        // Definisco il left panel, che contiene i componenti necessari relativi al login/registrazione
        JPanel left_panel = new JPanel();
        // Imposto le preferenze
        left_panel.setLayout(new GridLayout(0, 1));
        left_panel.setPreferredSize(new Dimension(255,300));
        left_panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        // Definisco i componenti da aggiungere
        JLabel label = new JLabel("Effettua l'accesso", JLabel.CENTER);
        label.setFont(UIManager.getFont("h1.regular.font"));
        JPanel register_panel = new JPanel(new FlowLayout());
        register_panel.add(new JLabel("Non sei ancora iscritto?"));
        register_panel.add(setUnderline(register_label));
        // Aggiungo i componenti
        left_panel.add(label);
        left_panel.add(new JLabel("Username:", JLabel.LEFT)).setFont(large_font);
        left_panel.add(username_field).setFont(large_font);
        left_panel.add(new JLabel("Password:", JLabel.LEFT)).setFont(large_font);
        left_panel.add(password_field).setFont(large_font);
        left_panel.add(login_button).setFont(large_font);
        left_panel.add(register_panel);

        // Definisco il right panel, che contiene i componenti necessari relativi alla ricerca
        JPanel right_panel = new JPanel();
        // Imposto le preferenze
        right_panel.setLayout(new GridLayout(0, 1, 0, 10));
        right_panel.setPreferredSize(new Dimension(550,300));
        right_panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        // Aggiungo i componenti
        right_panel.add(new JLabel("Cerca hotel", JLabel.LEFT)).setFont(h0_font);
        right_panel.add(search_field).setFont(h2_regular_font);
        right_panel.add(new JLabel("a", JLabel.LEFT)).setFont(h0_font);
        right_panel.add(search_combobox).setFont(h2_regular_font);
        right_panel.add(search_button).setFont(h3_regular_font);

        // Aggiungo tutto al panel principale di base
        add(welcome_panel, BorderLayout.PAGE_START);
        add(left_panel, BorderLayout.LINE_START);
        add(new JSeparator(SwingConstants.VERTICAL), BorderLayout.CENTER);
        add(right_panel, BorderLayout.LINE_END);
        add(Box.createVerticalStrut(10), BorderLayout.PAGE_END);

        // Inizializzo i componenti
        clear();
    }

    /**
     * <p align="justify">
     *     Definisce gli elementi necessari per la gestione del comportamento dei componenti in base all'interazione
     *     dell'utente con questi.
     * </p>
     */
    private void defineComponentsBehavior() {
        username_field.setInputVerifier(new CustomInputVerifier());
        password_field.setInputVerifier(new CustomInputVerifier());
        username_field.addCaretListener(new CustomCaretListener());
        username_field.addKeyListener(new PerformAction(login_button));
        password_field.addFocusListener(new PasswordFocusListener());
        password_field.addCaretListener(new CustomCaretListener());
        password_field.addKeyListener(new PerformAction(login_button));
        search_field.addCaretListener(new SearchCaretListener());
        search_field.addKeyListener(new PerformAction(search_button));
        search_combobox.setRenderer(new CityComboBoxRender());
        search_combobox.addItemListener(new SearchItemListener());
        search_combobox.getEditor().getEditorComponent().addFocusListener(new SearchFocusAdapter());
        search_combobox.getEditor().getEditorComponent().addKeyListener(new PerformAction(search_button));
        login_button.addActionListener(new LoginActionListener());
        register_label.addMouseListener(new RegisterMouseAdapter());
        search_button.addActionListener(new SearchActionListener());
    }


    /**
     * <p align="justify">
     *     Imposta i campi ai loro valori di default.
     * </p>
     */
    public void clear() {
        username_field.setText("");
        password_field.setText("");
        setShowRevealIcon(password_field, false);
        search_field.setText("");
        search_combobox.setSelectedIndex(-1);
        login_button.setEnabled(false);
        search_button.setEnabled(false);
        frame.requestFocusInWindow();
    }


    // Elementi per la gestione dei componenti durante l'interazione
    /**
     * {@link FocusListener} per il campo password
     * <p align="justify">
     *     Gestisce la visualizzazione dell'icona che consente di mostrare la password in chiaro.
     * </p>
     */
    class PasswordFocusListener implements FocusListener {
        @Override
        public void focusGained(FocusEvent e) { setShowRevealIcon(password_field, true); }

        @Override
        public void focusLost(FocusEvent e) { if (isBlank(password_field)) setShowRevealIcon(password_field, false); }
    }

    /**
     * <p align="justify">
     *     {@link FocusAdapter} per il campo di ricerca relativo alle città
     * </p> <p align="justify">
     *     Gestisce la visualizzazione del menu a tendina per la scelta della città quando l'elemento acquisisce il focus.
     * </p>
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
     * {@link CaretListener} per i campi username e password
     * <p align="justify">
     *     Gestisce il pulsante di login in base al contenuto dei campi username e password.
     * </p>
     */
    class CustomCaretListener implements CaretListener {

        @Override
        public void caretUpdate(CaretEvent e) {
            // Abilito il pulsante di login soltanto quando entrambi i campi sono non blank
            login_button.setEnabled(!isBlank(username_field) && !isBlank(password_field));
        }
    }

    /**
     * {@link CaretListener} per il campo di ricerca relativo all'hotel
     * <p align="justify">
     *     Gestisce la visualizzazione dell'icona che consente di cancellare rapidamente il contenuto del campo.
     * </p>
     */
    class SearchCaretListener implements CaretListener {
        @Override
        public void caretUpdate(CaretEvent e) {
            search_field.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, e.getDot() > 0);
        }
    }

    /**
     * {@link MouseAdapter} per l'etichetta di registrazione
     * <p align="justify">
     *     Consente la visualizzazione della finestra dedicata alla registrazione.
     * </p>
     */
    class RegisterMouseAdapter extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            new RegistrationDialog(frame, true, username_field, password_field);
        }
    }

    /**
     * {@link ActionListener} per il pulsante di login
     * <p align="justify">
     *     Gestisce il comportamento del pulsante corrispondente.
     * </p>
     */
    class LoginActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Se l'accesso va a buon fine, pulisco il panel
            if (frame.loginClient(username_field.getText(), new String(password_field.getPassword()))) clear();
        }
    }

    /**
     * {@link ActionListener} per il pulsante di ricerca
     * <p align="justify">
     *     Gestisce il comportamento del pulsante corrispondente.
     * </p>
     */
    class SearchActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // A seconda del contenuto dei due campi dedicati alla ricerca, scelgo i parametri per la ricerca
            if (isBlank(search_combobox)) frame.searchClient(null, null);
            else if (isBlank(search_field)) frame.searchClient(null, search_combobox.getSelectedItem().toString());
            else frame.searchClient(search_field.getText(), search_combobox.getSelectedItem().toString());
            clear();
        }
    }
}