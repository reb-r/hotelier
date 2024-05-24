package Client.GUI;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.event.*;

import static Client.GUI.CustomFrame.*;
import static Client.GUI.CustomFrame.NamePanel.*;


/**
 * <p align="justify">
 *     La classe {@code LoginDialog} estende la classe {@link JDialog} ed è la finestra dedicata al login.
 * </p>
 */
public class LoginDialog extends JDialog {
    /** Parent frame */
    final CustomFrame frame;
    /** Campo per il nome utente */
    private final JTextField username_field = new JTextField();
    /** Campo per la password */
    private final JPasswordField password_field = new JPasswordField();
    /** Pulsante di login */
    private final JButton login_button = new JButton("Accedi");
    /** Pulsante di registrazione */
    private final JLabel register_label = new JLabel("Registrati");

    public LoginDialog(CustomFrame parent, boolean modal) {
        // Chiamo il costruttore della superclasse e assegno i parametri
        super(parent, "Login", modal);
        frame = parent;

        // Imposto le preferenze delle variabili e gli elementi aggiuntivi
        setLayout(new BorderLayout(0, 20));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        username_field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nome utente");
        password_field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Password");
        login_button.setEnabled(false);
        defineComponentsBehavior();

        // Definisco un panel contenitore per alcune componenti e imposto le preferenze
        JPanel container = new JPanel(new GridLayout(0, 1));
        container.setPreferredSize(new Dimension(220, 220));
        // Definisco un panel aggiuntivo per la registrazione e aggiungo i componenti
        JPanel register_panel = new JPanel();
        register_panel.add(new JLabel("o"));
        register_panel.add(setUnderline(register_label));
        // Aggiungo i componenti al container
        container.add(new JLabel("Username:", JLabel.LEFT)).setFont(large_font);
        container.add(username_field).setFont(large_font);
        container.add(new JLabel("Password:", JLabel.LEFT)).setFont(large_font);
        container.add(password_field).setFont(large_font);
        container.add(login_button).setFont(large_font);
        container.add(register_panel);

        // Definisco i componenti da aggiungere
        JLabel title_label = new JLabel("Accesso", JLabel.LEFT);
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
        username_field.addCaretListener(new CustomCaretListener());
        username_field.addKeyListener(new PerformAction(login_button));
        password_field.addFocusListener(new PasswordFocusListener());
        password_field.addCaretListener(new CustomCaretListener());
        password_field.addKeyListener(new PerformAction(login_button));
        login_button.addActionListener(new LoginActionListener());
        register_label.addMouseListener(new LabelMouseAdapter());
    }


    // Elementi per la gestione dei componenti durante l'interazione
    /** @see HomePanel.PasswordFocusListener */
    class PasswordFocusListener implements FocusListener {
        @Override
        public void focusGained(FocusEvent e) { setShowRevealIcon(password_field, true); }

        @Override
        public void focusLost(FocusEvent e) { if (isBlank(password_field)) setShowRevealIcon(password_field, false); }
    }

    /** @see HomePanel.CustomCaretListener */
    class CustomCaretListener implements CaretListener {
        @Override
        public void caretUpdate(CaretEvent e) {
            login_button.setEnabled(e.getDot() > 0 && !isBlank(username_field) && !isBlank(password_field));
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
            // Se il login va a buon fine, chiudo la finestra corrente
            if (frame.loginClient(username_field.getText(), new String(password_field.getPassword()))) {
                ((JComponent) e.getSource()).getParent().setVisible(false);
                dispose();
            }
        }
    }

    /**
     * {@link MouseAdapter} per l'etichetta di registrazione
     * <p align="justify">
     *     Gestisce il comportamento del pulsante corrispondente.
     * </p>
     */
    class LabelMouseAdapter extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            new RegistrationDialog(frame, true, username_field, password_field);
            // Quando viene chiusa la finestra per la registrazione, controllo se è stato effettuato l'accesso
            // e chiudo la finestra corrente
            if (frame.getActivePanel() == SESSION_PANEL) {
                ((JComponent) e.getSource()).getParent().setVisible(false);
                dispose();
            }
        }
    }
}