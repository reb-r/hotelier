package Client.GUI;

import Server.Database.Review;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static Client.GUI.CustomFrame.*;
import static javax.swing.ScrollPaneConstants.*;


/**
 * <p align="justify">
 *     La classe {@code ReviewShowDialog} estende la classe {@link JDialog} ed è la finestra dedicata alla
 *     visualizzazione delle recensioni riferite ad un hotel.
 * </p>
 */
public class ReviewsDialog extends JDialog {
    static final Icon thumb = createImageIcon(path + "icons/thumb.png", 14);

    /** Parent frame */
    final CustomFrame frame;
    /** Panel dedicato alle recensioni */
    private final JPanel reviews_panel = new JPanel();
    /** Componente per la gestione dello scroll */
    private final JScrollPane scrollPane = new JScrollPane(reviews_panel, VERTICAL_SCROLLBAR_NEVER, HORIZONTAL_SCROLLBAR_NEVER);

    public ReviewsDialog(CustomFrame parent, String hotel, String[] reviews) {
        // Chiamo il costruttore della superclasse, assegno i parametri e inizializzo le variabili
        super(parent, hotel + " reviews", false);
        frame = parent;

        // Imposto le preferenze delle variabili e gli elementi aggiuntivi
        setLayout(new BorderLayout(0, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        scrollPane.putClientProperty(FlatClientProperties.STYLE, "borderWidth:0");
        scrollPane.getVerticalScrollBar().setUnitIncrement(15);
        reviews_panel.setLayout(new BoxLayout(reviews_panel, BoxLayout.PAGE_AXIS));
        reviews_panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 5));
        reviews_panel.setBackground(GRAY_COLOR);

        // Definisco un panel contenitore per alcune componenti e imposto le preferenze
        JPanel container = new JPanel(new BorderLayout());
        container.setPreferredSize(new Dimension(280, 473));
        // Aggiungo i componenti
        container.add(scrollPane);
        container.add(scrollPane.getVerticalScrollBar(), BorderLayout.LINE_END);

        JLabel title_label = new JLabel("Recensioni", JLabel.LEFT);
        title_label.setFont(h1_regular_font);

        // Aggiungo tutto al panel principale di base
        add(title_label, BorderLayout.PAGE_START);
        add(container);

        // Aggiungo le recensioni
        for (String review: reviews) addReview(review);
        if (reviews.length == 1) reviews_panel.add(Box.createVerticalStrut(80));
        else reviews_panel.add(Box.createVerticalGlue());

        // Imposto le proprietà del frame
        pack();
        setResizable(false);
        setLocation(parent.getX() - getWidth()/2 + 10, parent.getY());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().requestFocusInWindow();
        setVisible(true);
    }

    private void addReview(String review) {
        // Identifico i singoli campi suddividendo la stringa
        String[] element = review.replaceAll("[{|}]", "").split(";");

        // Definisco il panel dedicato alla recensione e imposto le preferenze
        JPanel review_panel = new JPanel();
        review_panel.setLayout(new BoxLayout(review_panel, BoxLayout.PAGE_AXIS));
        review_panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        review_panel.setPreferredSize(new Dimension(200, 230));
        review_panel.setAlignmentX(LEFT_ALIGNMENT);
        review_panel.putClientProperty(FlatClientProperties.STYLE, "arc:20");

        // Definisco il pulsante per la votazione
        JButton upvote_button;

        for (int field = 1; field < Review.getnFields(); field++) {
            switch (field) {
                // (ID -> case 0)
                // AUTORE
                case 1:
                    JPanel author_panel = new JPanel();
                    author_panel.setLayout(new BoxLayout(author_panel, BoxLayout.LINE_AXIS));
                    author_panel.setAlignmentX(LEFT_ALIGNMENT);
                    JLabel label = new JLabel("Pubblicata da " + element[field].trim(), JLabel.LEFT);
                    label.putClientProperty(FlatClientProperties.STYLE, "font: 120% $semibold.font");
                    author_panel.add(label);
                    author_panel.add(Box.createHorizontalGlue());
                    review_panel.add(author_panel).setFont(large_font);
                    break;
                // HOTEL -> case 2
                // RATE
                case 3:
                    review_panel.add(new JLabel("Punteggio complessivo")).setFont(large_font);
                    review_panel.add(addScore(element[field].trim(), "", true));
                    review_panel.add(Box.createVerticalStrut(3));
                    break;
                // RATINGS
                case 4:
                    review_panel.add(new JLabel("Punteggi per categoria")).setFont(large_font);
                    displayRatings(review_panel, element[field].replaceAll("[\\[|\\]]", "").split(","));
                    review_panel.add(Box.createVerticalStrut(1));
                    break;
                // DATA
                case 5:
                    review_panel.add(new JLabel("Data di pubblicazione"));
                    review_panel.add(new JLabel(element[field].trim()));
                    break;
                // VOTI
                case 6:
                    upvote_button = new JButton(element[field].trim(), thumb);
                    upvote_button.setPreferredSize(new Dimension(50, 30));
                    upvote_button.setMaximumSize(new Dimension(50, 30));
                    upvote_button.setMargin(new Insets(2, 2, 2, 2));
                    upvote_button.setToolTipText("Vota questa recensione");
                    upvote_button.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS);
                    upvote_button.putClientProperty(FlatClientProperties.STYLE, "hoverBackground:#d3d3d3");
                    upvote_button.addActionListener(new UpvoteActionListener(element[0].trim()));
                    review_panel.add(Box.createVerticalStrut(3));
                    review_panel.add(upvote_button);
                    break;
                default: break;
            }

            // Aggiungo il panel della recensione al panel di tutte le recensioni
            reviews_panel.add(review_panel);
            reviews_panel.add(Box.createVerticalStrut(3));
        }
    }


    // Elementi per la gestione dei componenti durante l'interazione
    /**
     * {@link ActionListener} per il pulsante per la votazione di una recensione
     * <p align="justify">
     *     Gestisce il comportamento del componente corrispondente.
     * </p>
     */
    class UpvoteActionListener implements ActionListener {
        /** Numero della recensione */
        String review_id;

        public UpvoteActionListener(String id) { review_id = id; }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Se la votazione ha successo, aggiorno l'indicatore
            if (frame.upvoteReviewClient(review_id)) {
                int upvotes = Integer.parseInt(((JButton) e.getSource()).getText()) + 1;
                ((JButton) e.getSource()).setText(String.valueOf(upvotes));
            }
        }
    }
}