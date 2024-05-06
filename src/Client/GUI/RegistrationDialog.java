package Client.GUI;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

import static Client.GUI.CustomFrame.*;


/**
 * <p align="justify">
 *     La classe {@code RegisterDialog} estende la classe {@link JDialog} ed è la finestra dedicata alla registrazione.
 * </p>
 */
public class RegistrationDialog extends JDialog {
    /** Parent frame */
    final CustomFrame frame;
    /** Campo per il nome utente */
    private final JTextField username_field = new JTextField("");
    /** Campo per la password */
    private final JPasswordField password_field = new JPasswordField("");
    /** Campo per la conferma password */
    private final JPasswordField confirm_field = new JPasswordField();
    /** Etichetta informativa */
    private final JLabel password_mismatch = new JLabel(" Attenzione: le password non corrispondono.");
    /** Pulsante di registrazione */
    private final JButton register_button = new JButton("Registrati");
    /** Pulsante per l'annullamento */
    private final JLabel return_label = new JLabel("Torna indietro");

    public RegistrationDialog(CustomFrame parent, Boolean modal, JTextField username, JPasswordField password) {
        // Chiamo il costruttore della superclasse, assegno i parametri e inizializzo le variabili
        super(parent, "Registration", modal);
        frame = parent;

        // Imposto le preferenze delle variabili e gli elementi aggiuntivi
        setLayout(new BorderLayout(0, 20));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        username_field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nome utente");
        password_field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Password");
        confirm_field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Conferma password");
        password_mismatch.setVerticalAlignment(JLabel.TOP);
        register_button.setEnabled(false);
        defineComponentsBehavior();
        setup(username, password);

        // Definisco un panel contenitore per alcune componenti e imposto le preferenze
        JPanel container = new JPanel(new GridLayout(0, 1));
        container.setPreferredSize(new Dimension(250, 350));
        // Definisco un panel aggiuntivo per l'annullamento e aggiungo i componenti
        JPanel return_panel = new JPanel();
        return_panel.add(new JLabel("o"));
        return_panel.add(setUnderline(return_label));
        // Aggiungo i componenti al container
        container.add(new JLabel("Username:", JLabel.LEFT)).setFont(large_font);
        container.add(username_field).setFont(large_font);
        container.add(new JLabel("Password:", JLabel.LEFT)).setFont(large_font);
        container.add(password_field).setFont(large_font);
        container.add(new JLabel("Conferma password:", JLabel.LEFT)).setFont(large_font);
        container.add(confirm_field).setFont(large_font);
        container.add(password_mismatch).setForeground(Color.WHITE);
        container.add(register_button).setFont(large_font);
        container.add(return_panel);

        // Definisco i componenti da aggiungere
        JLabel title_label = new JLabel("Registrazione", JLabel.LEFT);
        title_label.setFont(h1_regular_font);

        // Aggiungo tutto al panel principale di base
        add(title_label, BorderLayout.PAGE_START);
        add(container);

        // Imposto le proprietà del frame
        pack();
        setResizable(false);
        setLocationRelativeTo(getOwner());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().requestFocusInWindow();
        setVisible(true);
    }

    private void defineComponentsBehavior() {
        username_field.setInputVerifier(new CustomInputVerifier());
        password_field.setInputVerifier(new CustomInputVerifier());
        username_field.addCaretListener(new UsernameCaretListener());
        username_field.addKeyListener(new PerformAction(register_button));
        password_field.addFocusListener(new PasswordFocusListener());
        password_field.addCaretListener(new PasswordCaretListener());
        password_field.addKeyListener(new PerformAction(register_button));
        confirm_field.addFocusListener(new ConfirmFocusListener());
        confirm_field.addCaretListener(new PasswordCaretListener());
        confirm_field.addKeyListener(new PerformAction(register_button));
        register_button.addActionListener(new RegisterActionListener());
        return_label.addMouseListener(new LabelMouseAdapter());
    }

    /**
     * <p align="justify">
     *     Imposta con i valori contenuti nei campi indicati, presenti nella finestra proprietaria di questo oggetto,
     *     i campi corrispondenti contenuti in questo oggetto.
     * </p>
     * @param username il campo del nome utente
     * @param password il campo della password
     */
    public void setup(JTextField username, JPasswordField password) {
        if (!isBlank(username)) {
            username_field.setText(username.getText());
            if (!isBlank(password)) {
                password_field.setText(new String(password.getPassword()));
                setShowRevealIcon(password_field, true);
            } else confirm_field.setEnabled(false);
        } else password.setText("");
    }


    // Elementi per la gestione dei componenti durante l'interazione
    /** @see HomePanel.PasswordFocusListener */
    class PasswordFocusListener implements FocusListener {
        @Override
        public void focusGained(FocusEvent e) { setShowRevealIcon(password_field, true); }

        @Override
        public void focusLost(FocusEvent e) { if (isBlank(password_field)) setShowRevealIcon(password_field, false); }
    }

    /**
     * {@link FocusListener} per il campo di conferma password
     * <p align="justify">
     *     Gestisce la visualizzazione dell'icona che consente di mostrare la password in chiaro e la segnalazione
     *     della mancata corrispondenza tra le password inserite.
     * </p>
     */
    class ConfirmFocusListener implements FocusListener {
        @Override
        public void focusGained(FocusEvent e) {
            setShowRevealIcon(confirm_field, true);
            // Cambio il colore dell'etichetta informativa per renderla dello stesso colore del bordo del campo 
            // che ha acquisito il focus
            if (confirm_field.getClientProperty(FlatClientProperties.OUTLINE) != null)
                password_mismatch.setForeground(ORANGE_COLOR);
        }

        @Override
        public void focusLost(FocusEvent e) {
            if (isBlank(confirm_field)) setShowRevealIcon(confirm_field, false);
            // Cambio il colore dell'etichetta informativa per renderla dello stesso colore del bordo del campo 
            // che ha perso il focus nel caso in cui il campo di conferma non sia vuoto
            else if (!Arrays.equals(confirm_field.getPassword(), password_field.getPassword())) {
                password_mismatch.setForeground(Color.ORANGE);
                confirm_field.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_WARNING);
            }
        }
    }

    /**
     * {@link CaretListener} per il campo username
     * <p align="justify">
     *     Gestisce il pulsante di registrazione in base al contenuto dei campi username, password e conferma password.
     * </p>
     */
    class UsernameCaretListener implements CaretListener {
        @Override
        public void caretUpdate(CaretEvent e) { register_button.setEnabled(check()); }
    }

    /**
     * {@link CaretListener} per i campi password
     * <p align="justify">
     *     Gestisce il pulsante di registrazione in base al contenuto dei campi username, password e conferma password.
     * </p> <p align="justify">
     *     Si occupa anche della segnalazione della mancata corrispondenza tra le password inserite.
     * </p>
     */
    class PasswordCaretListener implements CaretListener {
        @Override
        public void caretUpdate(CaretEvent e) {
            // Se il campo di conferma password non è vuoto, faccio ulteriori controlli, altrimenti imposto a default
            if (!isBlank(confirm_field)) {
                // Se i contenuti dei due campi dedicati alle password non corrispondono, a seconda di chi possiede
                // il focus scelgo il colore dell'etichetta informativa per renderla dello stesso colore del bordo
                // del campo di conferma password, altrimenti imposto a default
                if (!Arrays.equals(confirm_field.getPassword(), password_field.getPassword())) {
                    if (confirm_field.isFocusOwner()) password_mismatch.setForeground(ORANGE_COLOR);
                    else password_mismatch.setForeground(Color.ORANGE);
                    confirm_field.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_WARNING);
                } else {
                    password_mismatch.setForeground(Color.WHITE);
                    confirm_field.putClientProperty(FlatClientProperties.OUTLINE, null);
                }
            } else {
                password_mismatch.setForeground(Color.WHITE);
                confirm_field.putClientProperty(FlatClientProperties.OUTLINE, null);
            }
            register_button.setEnabled(check());
        }
    }

    /**
     * <p align="justify">
     *     Controlla se ci sono dei campi vuoti e se i contenuti dei campi dedicati alle password corrispondono.
     * </p>
     * @return {@code true} se tutti i campi, di username, password e conferma password, non sono vuoti e le password
     * corrispondono
     */
    private boolean check() {
        return !isBlank(username_field) && !isBlank(password_field) && !isBlank(confirm_field)
                && Arrays.equals(password_field.getPassword(), confirm_field.getPassword());
    }

    /**
     * {@link ActionListener} per il pulsante di registrazione
     * <p align="justify">
     *     Gestisce il comportamento del pulsante corrispondente.
     * </p>
     */
    class RegisterActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = username_field.getText();
            String password = new String(password_field.getPassword());
            // Se la registrazione va a buon fine, chiudo la finestra corrente ed effettuo l'accesso automaticamente
            if (frame.registerClient(username_field.getText(), password)) {
                ((JComponent) e.getSource()).getParent().setVisible(false);
                dispose();
                frame.loginClient(username, password);
            }
        }
    }

    /**
     * {@link MouseAdapter} per l'etichetta di annullamento
     * <p align="justify">
     *     Gestisce il comportamento del pulsante corrispondente.
     * </p>
     */
    class LabelMouseAdapter extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            ((JComponent) e.getSource()).getParent().setVisible(false);
            dispose();
        }
    }
}