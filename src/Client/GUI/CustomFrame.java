package Client.GUI;

import Client.GUIHOTELIERCustomerClient;
import Server.Database.Hotel.City;
import Server.Database.Hotel.Ratings;
import com.formdev.flatlaf.FlatClientProperties;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static Client.GUI.CustomFrame.NamePanel.*;
import static javax.swing.JOptionPane.*;


/**
 * <p align="justify">
 *     La classe {@code CustomFrame} estende la classe {@link JFrame} e costituisce il frame principale, usato come base
 *     per la visualizzazione delle pagine.
 * </p>
 */
public class CustomFrame extends JFrame {
    // Insieme di elementi personalizzati di uso generale
    static final Color ORANGE_COLOR = new Color(226, 165, 58);
    static final Color GRAY_COLOR = new Color(221, 221, 221);
    // static final Color RED_COLOR = new Color(255, 57, 47);
    static final Color BLUE_COLOR = new Color(0, 122, 255);
    static final Font large_font = UIManager.getFont("large.font");
    static final Font h0_font = UIManager.getFont("h0.font");
    static final Font h1_font = UIManager.getFont("h1.font");
    static final Font h1_regular_font = UIManager.getFont("h1.regular.font");
    static final Font h2_regular_font = UIManager.getFont("h2.regular.font");
    static final Font h3_regular_font = UIManager.getFont("h3.regular.font");
    static final String path = "src/Client/config/";
    static final Icon full_circle = createImageIcon(path + "icons/circle-full.png", 12);
    static final Icon half_circle = createImageIcon(path + "icons/circle-half.png", 12);
    static final Icon empty_circle = createImageIcon(path + "icons/circle-empty.png", 12);
    static final Icon full_circle_small = createImageIcon(path + "icons/circle-full.png", 10);
    static final Icon half_circle_small = createImageIcon(path + "icons/circle-half.png", 10);
    static final Icon empty_circle_small = createImageIcon(path + "icons/circle-empty.png", 10);

    /** Panel contenitore */
    private final JPanel container;

    /** <p align="justify"> Layout con cui viene gestita la visualizzazione delle diverse pagine </p> */
    private final CardLayout panels = new CardLayout();

    /** Pagina con vista da guest */
    private final GuestPanel guest_panel;

    /** Pagina con vista da utente loggato */
    private final SessionPanel session_panel;

    /** Nome della pagina corrente attiva */
    private NamePanel activePanel;

    /**
     * <p align="justify"> Cronologia che tiene traccia degli ultimi elementi inseriti per la ricerca </p>
     * <p align="justify"> La posizione {@code 0} si riferisce al nome dell'hotel, mentre la posizione {@code 1} alla città. </p>
     */
    private final String[] history = new String[2];

    /** Istanza di client per l'interazione col server */
    GUIHOTELIERCustomerClient client;


    public CustomFrame(GUIHOTELIERCustomerClient client) throws IOException, FontFormatException {
        // Assegno i parametri alle variabili e le inizializzo
        this.client = client;
        container = new JPanel(panels);

        // Definisco il titolo del frame
        setTitle("HOTELIER");

        // Imposto le preferenze e i bordi del container che occupa il frame
        container.setPreferredSize(new Dimension(850, 540));
        container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Dimension dimension = new Dimension(830, 520);
        // Definisco i panels e li aggiungo al container
        container.add(new HomePanel(this), HOME_PANEL.name); // home page
        container.add(guest_panel = new GuestPanel(this, dimension), GUEST_PANEL.name);
        container.add(session_panel = new SessionPanel(this, dimension), SESSION_PANEL.name);

        // Aggiungo il container al frame
        add(container);

        // Imposto le proprietà del frame
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getContentPane().requestFocusInWindow();
        setVisible(true);

        panels.show(container, (activePanel = HOME_PANEL).name);
    }


    // Metodi get e set
    /**
     * Restituisce la pagina corrente attivo.
     * @return il panel corrente attivo
     */
    NamePanel getActivePanel() { return activePanel; }

    /**
     * <p align="justify">
     *     Imposta la cronologia sull'ultima ricerca effettuata andata a buon fine.
     * </p>
     * @param hotel l'hotel con cui aggiornare la cronologia
     * @param city la città con cui aggiornare la cronologia
     */
    private void setHistory(String hotel, String city) {
        history[0] = hotel;
        history[1] = city;
    }


    // Altri metodi
    /** Mostra la home page. */
    void showHome_panel() {
        panels.show(container, (activePanel = HOME_PANEL).name); // imposta come attivo e visualizza la home
        getContentPane().requestFocusInWindow();
    }

    /** Mostra la vista da guest. */
    void showGuest_panel() {
        panels.show(container, (activePanel = GUEST_PANEL).name); // imposta come attiva e visualizza la vista da guest
        guest_panel.setSelectedTab(1); // visualizza la tab dedicata alla ricerca
        getContentPane().requestFocusInWindow();
    }

    /** Mostra la vista da utente loggato. */
    void showSession_panel() {
        panels.show(container, (activePanel = SESSION_PANEL).name); // imposta come attiva e visualizza la vista da utente
        // Se nella vista da guest era stata effettuata una ricerca in precedenza ed era visualizzata la tab corrispondente,
        // mostro la stessa tab nella nuova vista, altrimenti mostro la home tab
        if (guest_panel.getSelectedTab() == 1) {
            searchClient(null, null);
            session_panel.setSelectedTab(1);
        } else session_panel.setSelectedTab(0);
        getContentPane().requestFocusInWindow();
    }


    // Metodi per la propagazione delle chiamate ai metodi dell'istanza del client che si occupano dell'interazione col server
    /**
     * <p align="justify">
     *     Consente la registrazione mediante l'invocazione del metodo dedicato del client.
     * </p> <p align="justify">
     *     Propaga il risultato restituito dal metodo invocato.
     * </p>
     * @param username il nome utente per la registrazione
     * @param password la password per la registrazione
     * @return {@code true} se la registrazione è andata a buon fine, {@code false} altrimenti
     */
    boolean registerClient(String username, String password) {
        return client.register(username, password);

    }

    /**
     * <p align="justify">
     *     Consente l'accesso mediante l'invocazione del metodo dedicato del client.
     * </p> <p align="justify">
     *     Propaga il risultato restituito dal metodo invocato.
     * </p>
     * @param username il nome utente per il login
     * @param password la password per il login
     * @return {@code true} se l'accesso è andato a buon fine, {@code false} altrimenti
     */
    boolean loginClient(String username, String password) {
        if (client.login(username, password)) {
            session_panel.setSession(username);
            showSession_panel();
            new NotificationDialog(this, true);
            return true;
        } else return false;
    }

    /**
     * <p align="justify">
     *     Consente la registrazione per il servizio di notifica tramite callback mediante l'invocazione del metodo
     *     dedicato del client.
     * </p>
     * @param cities la lista di città selezionate per la ricezione delle notifiche per l'aggiornamento della classifica
     */
    void registerForCallbackClient(List<String> cities) {
        if (!cities.isEmpty()) client.registerForCallback(cities);
        else showMessageDialog(this, "Non hai selezionato alcuna città di interesse.\n"
                        + "Se desideri modificare quest'impostazione dovrai effettuare il logout e accedere nuovamente.",
                "Ranking update notification", INFORMATION_MESSAGE);
    }

    /**
     * <p align="justify">
     *     Consente il logout mediante l'invocazione del metodo dedicato del client.
     * </p> <p align="justify">
     *     Propaga il risultato restituito dal metodo invocato.
     * </p>
     * @return {@code true} se il logout è andato a buon fine, {@code false} altrimenti
     */
    boolean logoutClient() {
        if (client.logout()) {
            showHome_panel(); // mostro la home page
            return true;
        } else return false;
    }

    /**
     * <p align="justify">
     *     Consente la ricerca mediante l'invocazione del metodo dedicato del client.
     * </p> <p align="justify">
     *     Se il parametro {@code city} è {@code null} verranno usati gli elementi della cronologia al posto dei parametri.
     * </p>
     * @param hotel il nome dell'hotel da cercare, o {@code null}
     * @param city la città in cui è situato l'hotel o in cui effettuare una ricerca totale, o {@code null}
     */
    void searchClient(String hotel, String city) {
        String[] results;
        if (city != null) {
            results = client.search(hotel, city);
            setHistory(hotel, city);
        } else results = client.search(history[0], history[1]);
        if (results != null) {
            if (activePanel == HOME_PANEL) showGuest_panel();
            if (activePanel == GUEST_PANEL) guest_panel.displaySearchResults(results);
            else session_panel.displaySearchResults(results);
        }
    }

    /**
     * <p align="justify">
     *     Consente la pubblicazione della recensione mediante l'invocazione del metodo dedicato del client.
     * </p>
     * @param hotel l'hotel da recensire
     * @param city la città in cui è situato l'hotel
     * @param score il punteggio sintetico
     * @param scores i punteggi per categoria
     */
    void insertReviewClient(String hotel, String city, double score, double[] scores) {
        client.insertReview(hotel, city, score, scores);
    }

    /**
     * <p align="justify">
     *     Consente la visualizzazione delle recensioni dell'hotel indicato mediante l'invocazione del metodo dedicato
     *     del client.
     * </p> <p align="justify">
     *     Propaga il risultato restituito dal metodo invocato.
     * </p>
     * @param hotel l'hotel di cui mostrare le recensioni
     * @param city la città in cui è situato l'hotel
     * @return la lista di recensioni pubblicate per questo hotel
     */
    String[] showReviewsClient(String hotel, String city) {
        return client.showReviews(hotel, city);
    }

    /**
     * <p align="justify">
     *     Consente di votare una recensione (visualizzata in precedenza) mediante l'invocazione del metodo dedicato
     *     del client.
     * </p> <p align="justify">
     *     Propaga il risultato restituito dal metodo invocato.
     * </p>
     * @return {@code true} se la votazione è andata a buon fine, {@code false} altrimenti
     */
    boolean upvoteReviewClient(String review) {
        return client.upvote(review);
    }

    /**
     * <p align="justify">
     *     Consente la visualizzazione delle recensioni pubblicate dall'utente loggato mediante l'invocazione del metodo
     *     dedicato del client.
     * </p> <p align="justify">
     *     Propaga il risultato restituito dal metodo invocato.
     * </p>
     * @return la lista di recensioni pubblicate dall'utente che ha effettuato l'accesso su questo client
     */
    String[] showMyReviewsClient() {
        return client.showMyReviews();
    }

    /**
     * <p align="justify">
     *     Consente la visualizzazione del badge dell'utente loggato mediante l'invocazione del metodo dedicato del client.
     * </p> <p align="justify">
     *     Propaga il risultato restituito dal metodo invocato.
     * </p>
     * @return il badge dell'utente che ha effettuato l'accesso su questo client
     */
    String showMyBadgesClient() {
        return client.showMyBadges();
    }


    // Metodi di uso generale per la personalizzazione e visualizzazione di elementi grafici
    /**
     * <p align="justify">
     *     Crea un'icona a partire dall'immagine il cui percorso è quello indicato e della dimensione indicata.
     * </p>
     * @param path il percorso del file immagine con cui creare l'icona
     * @param dimension la dimensione ({@code width} e {@code height}) dell'icona
     * @return l'icona generata tramite l'immagine
     */
    static ImageIcon createImageIcon(String path, int dimension) {
        try {
            BufferedImage image = ImageIO.read(new File(path));
            Image custom_image = image.getScaledInstance(dimension, dimension, Image.SCALE_SMOOTH);
            return new ImageIcon(custom_image);
        } catch (IOException ignored) { }
        return new ImageIcon();
    }

    /**
     * <p align="justify">
     *     Imposta il testo dell'etichetta indicata come sottolineato e modifica il cursore, scegliendo un
     *     {@link Cursor#HAND_CURSOR}.
     * </p>
     * @param label l'etichetta che si desidera modificare
     * @return l'etichetta modificata
     */
    static JLabel setUnderline(JLabel label) {
        Font font = label.getFont();
        Map attributes = font.getAttributes();
        attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        label.setFont(font.deriveFont(attributes));
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return label;
    }

    /**
     * <p align="justify">
     *     Gestisce la visualizzazione dell'icona per visualizzare la password in chiaro del componente indicato.
     * </p>
     * @param password_field il componente dedicato all'inserimento della password
     * @param showIcon il valore con cui impostare l'icona
     */
    static void setShowRevealIcon(JPasswordField password_field, boolean showIcon) {
        if (showIcon) password_field.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true");
        else password_field.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: false");
    }


    /**
     * <p align="justify">
     *     Inserisce i punteggi per categoria nel panel indicato, secondo uno schema predefinito.
     * </p>
     * @param panel il panel in cui visualizzare i punteggi per categoria
     * @param ratings i punteggi per categoria da visualizzare
     */
    static void displayRatings(JPanel panel, String[] ratings) {
        for (int j = 0; j < Ratings.getnCategories(); j++) {
            switch (j) {
                case 0: panel.add(addScore(ratings[j].trim(), "Posizione", false));
                    break;
                case 1: panel.add(addScore(ratings[j].trim(), "Pulizia", false));
                    break;
                case 2: panel.add(addScore(ratings[j].trim(), "Servizio", false));
                    break;
                case 3: panel.add(addScore(ratings[j].trim(), "Qualità-prezzo", false));
                    break;
            }
        }
    }

    /**
     * <p align="justify">
     *     Visualizza graficamente il punteggio indicato mediante icone.
     * </p>
     * @param score il punteggio da visualizzare
     * @param rating il rating a cui fa riferimento il punteggio
     * @param rate un valore che se a {@code true} indica che il {@code rating} è il punteggio sintetico
     * @return il panel contenente il punteggio visualizzato graficamente
     */
    static JPanel addScore(String score, String rating, boolean rate) {
        // Definisco il panel che visualizzerà graficamente il punteggio e imposto le preferenze
        JPanel points_panel = new JPanel();
        points_panel.setLayout(new BoxLayout(points_panel, BoxLayout.LINE_AXIS));
        points_panel.setAlignmentX(LEFT_ALIGNMENT);
        points_panel.setToolTipText("Punteggio " + score + " su 5");

        double points = Double.parseDouble(score);
        if (rate) points_panel.add(new JLabel(score + " ")).setFont(large_font);

        // Per un punteggio la cui parte frazionaria è compresa nel range (0.7, 1) verrà considerato un punteggio
        // ausiliario "arrotondato" all'intero successivo, mentre per un punteggio la cui parte frazionaria è compresa
        // nel range [0, 0.3) verrà considerato un punteggio ausiliario "arrotondato" all'intero precedente.
        // Per ciascun punteggio viene visualizzato un cerchio pieno per ogni intero che costituisce la parte intera
        // dello stesso e successivamente per i punteggi la cui parte frazionaria è compresa nel range [0.3, 0.7] verrà
        // mostrato un cerchio a metà.
        // In seguito se non si è ancora raggiunto il massimo valore possibile per un punteggio verranno visualizzati
        // tanti cerchi vuoti quanto è la differenza tra il numero di cerchi inseriti e il massimo valore possibile.

        // Modalità iterativa
        /*
        int counter = 0;
        while (points > counter + 0.7) {
            if (rate) points_panel.add(new JLabel(full_circle));
            else points_panel.add(new JLabel(full_circle_small));
            counter++;
        }
        if (points >= counter + 0.3) {
            if (rate) points_panel.add(new JLabel(half_circle));
            else points_panel.add(new JLabel(half_circle_small));
            counter++;
        }
        if (rate) for (int i = 0; i < (5 - counter); i++) points_panel.add(new JLabel(empty_circle));
        else for (int i = 0; i < (5 - counter); i++) points_panel.add(new JLabel(empty_circle_small));
        */

        // Modalità ricorsiva
        if (rate) addBigPoints(points_panel, points, 0);
        else addPoints(points_panel, points, 0);

        points_panel.add(new JLabel(" " + rating));
        return points_panel;
    }

    /**
     * <p align="justify">
     *     Gestisce ricorsivamente l'inserimento delle icone per la visualizzazione grafica del punteggio indicato.
     * </p>
     * @param panel il panel contenente il punteggio visualizzato graficamente
     * @param score il punteggio attribuito
     * @param counter un contatore ausiliario, di norma inizialmente a {@code 0}
     */
    private static void addPoints(JPanel panel, double score, int counter) {
        if (score > counter) {
            if (score > counter + 0.7) panel.add(new JLabel(full_circle_small));
            else if (score >= counter + 0.3) panel.add(new JLabel(half_circle_small));
            else panel.add(new JLabel(empty_circle_small));
            addPoints(panel, score, ++counter);
        } else for (int i = 0; i < 5 - counter; i++) panel.add(new JLabel(empty_circle_small));
    }

    /**
     * @implNote {@link #addPoints} con icone grandi
     */
    private static void addBigPoints(JPanel panel, double score, int counter) {
        if (score > counter) {
            if (score > counter + 0.7) panel.add(new JLabel(full_circle));
            else if (score >= counter + 0.3) panel.add(new JLabel(half_circle));
            else panel.add(new JLabel(empty_circle));
            addBigPoints(panel, score, ++counter);
        } else for (int i = 0; i < 5 - counter; i++) panel.add(new JLabel(empty_circle));
    }


    // Metodi di uso generale
    /**
     * <p align="justify">
     *     Determina se il campo del componente indicato dedicato all'inserimento del testo è blank, cioè vuoto o
     *     contiene solo spazi.
     * </p>
     * @param component il componente su cui effettuare la verifica
     * @return {@code true} se il componente è blank, {@code false} altrimenti
     */
    static boolean isBlank(JComponent component) {
        if (component.getClass().equals(JPasswordField.class))
            return new String(((JPasswordField) component).getPassword()).isBlank();
        else if (component.getClass().equals(JTextField.class)) return ((JTextField) component).getText().isBlank();
        else return ((JComboBox) component).getSelectedIndex() == -1;
    }

    /**
     * <p align="justify">
     *     Restituisce la lista completa di tutti gli hotel presenti, in ordine di città e nome.
     * </p>
     * @return la lista di hotel in ordine per città e nome
     */
    public String[] getHotelValues() {
        String[] results;
        LinkedList<String> hotelList = new LinkedList<>();
        for (City city: City.values()) {
            results = client.search(null, city.getName());
            for (String hotel: results)
                hotelList.add(hotel.replaceAll("[{|}]", "").split(";")[0]);
        }
        return hotelList.toArray(new String[]{""});
    }


    // Elementi per la gestione dei componenti durante l'interazione
    /**
     * {@link KeyListener} per i campi d'inserimento
     * <p align="justify">
     *     Gestisce gli eventi legati alla pressione del tasto di invio per simulare un click effettuato sul pulsante
     *     associato.
     * </p>
     */
    static class PerformAction implements KeyListener {
        /** Pulsante su cui simulare l'azione */
        private final JButton button;

        PerformAction(JButton button) { this.button = button; }

        @Override
        public void keyTyped(KeyEvent e) { }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) if (button.isEnabled()) button.doClick();
        }

        @Override
        public void keyReleased(KeyEvent e) { }
    }

    /**
     * {@link ListCellRenderer} per il campo dedicato alla scelta della città
     * <p align="justify">
     *     Gestisce la corretta visualizzazione della scelta effettuata tramite il {@code ComboBox}.
     * </p>
     */
    static class CityComboBoxRender extends JLabel implements ListCellRenderer<Object> {

        private static final String title = "Città"; // testo di default visualizzato
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            // Se non è stato scelto nessun elemento visualizzo il testo di default,
            // altrimenti visualizzo l'elemento scelto
            if (index == -1 || value == null) setText(title);
            else setText(value.toString());
            return this;
        }

    }

    /**
     * {@link InputVerifier} per {@link JTextField} e {@link JPasswordField} dedicati rispettivamente a username e password
     * Convalida l'input inserito.
     */
    static class CustomInputVerifier extends InputVerifier {
        @Override
        public boolean verify(JComponent input) {
            // Se il componente è un JTextField
            if (input.getClass().equals(JTextField.class)) {
                // Controllo se l'input contiene spazi e lo resetto a vuoto, altrimenti controllo che rispetti i vincoli
                // Se non è così, l'input non è valido
                if (isBlank(input)) ((JTextField) input).setText("");
                else if (((JTextField) input).getText().matches("[ \"]")) {
                    showMessageDialog(input.getRootPane(), "Sono presenti caratteri non ammessi!",
                            "Input error", ERROR_MESSAGE);
                    return false;
                }
            } else {
                // Controllo se l'input contiene spazi e lo resetto a vuoto, altrimenti controllo che rispetti i vincoli
                // Se non è così, l'input non è valido
                if (isBlank(input)) ((JPasswordField) input).setText("");
                else if (new String(((JPasswordField) input).getPassword()).contains(" ")) {
                    showMessageDialog(input.getRootPane(), "In questo campo non sono ammessi spazi!",
                            "Input error", ERROR_MESSAGE);
                    return false;
                } else if (((JPasswordField) input).getPassword().length < 5
                        && input.getParent().getClass().equals(RegistrationDialog.class)) {
                    showMessageDialog(input.getRootPane(), "La password deve essere lunga almeno 5 caratteri.",
                            "Input warning", WARNING_MESSAGE);
                    return false;
                }
            }
            // In tutti gli altri casi l'input è valido
            return true;
        }
    }


    /**
     * <p align="justify">
     *     La classe {@code NamePanel} rappresenta l'insieme di pagine che si alternano nella visualizzazione su schermo.
     * </p>
     * @see #HOME_PANEL
     * @see #GUEST_PANEL
     * @see #SESSION_PANEL
     */
    public enum NamePanel {
        /** Home */
        HOME_PANEL("HomePanel"),
        /** Guest */
        GUEST_PANEL("GuestPanel"),
        /** User */
        SESSION_PANEL("SessionPanel");

        private final String name;

        NamePanel(String name) {
            this.name = name;
        }
    }
}