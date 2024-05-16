package Client;

import MyExceptions.InvalidPasswordException;
import MyExceptions.UsernameAlreadyTakenException;
import Client.GUI.CustomFrame;
import Server.Database.Hotel.Hotel;
import Server.Message;

import com.formdev.flatlaf.themes.FlatMacLightLaf;
import java.awt.*;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import javax.swing.*;

import static Client.HOTELIERClient.Command.*;
import static Server.Message.Reply.Info.*;
import static Server.Message.Reply.Status.*;
import static Server.Message.Request;
import static javax.swing.JOptionPane.*;


/**
 * <p align="justify">
 *     La classe {@code GUIHOTELIERCustomerClient} estende la classe astratta {@link HOTELIERClient} e gestisce
 *     l'interazione con l'utente tramite GUI.
 * </p>
 */
public class GUIHOTELIERCustomerClient extends HOTELIERClient {
    /** GUI frame */
    CustomFrame frame;

    public GUIHOTELIERCustomerClient(String server_address, int connection_port, int registry_port, String multicast_address,
                                     int multicast_port) throws IOException, NotBoundException {
        super(server_address, connection_port, registry_port);
        multicast = new MulticastClient(multicast_address, multicast_port, frame);

        // Imposto le preferenze per gli elementi indicati della GUI
        FlatMacLightLaf.setup();
        UIManager.put("OptionPane.messageAreaBorder", BorderFactory.createEmptyBorder(5, 5, 5, 5));
        UIManager.put("Frame.arc", 5);
        UIManager.put("TextComponent.arc", 5);
        UIManager.put("ScrollPane.arc", 20);
        UIManager.put("ScrollPane.smoothScrolling", true);
        UIManager.put("ScrollBar.showButtons", true);
    }

    @Override
    public void run() {
        try {
            frame = new CustomFrame(this);
        } catch (IOException | FontFormatException e) {
            System.out.println("An error occurred while configuring GUI. Please retry.");
            System.exit(1);
        }
    }

    @Override
    public synchronized void notifyEvent(String city, List<String> hotels) throws RemoteException {
        boolean[] updated = rankingUpdate(city, hotels);
        showMessageDialog(null, "Notifica evento: aggiornamento classifica di " + city + "\n"
                        + "1. " + hotels.get(0) + ((updated[0])? " (NEW)\n" : "\n")
                        + "2. " + hotels.get(1) + ((updated[1])? " (NEW)\n" : "\n")
                        + "3. " + hotels.get(2) + ((updated[2])? " (NEW)\n" : "\n")
                        + "4. " + hotels.get(3) + ((updated[3])? " (NEW)\n" : "\n")
                        + "5. " + hotels.get(4) + ((updated[4])? " (NEW)\n" : "\n"),
                "Ranking update notification", JOptionPane.INFORMATION_MESSAGE);
    }


    /**
     * <p align="justify">
     *     Controlla quali hotel hanno subito un cambio di posizione in classifica in seguito alla ricezione di una
     *     notifica e aggiorna la classifica.
     * </p>
     * @param hotels la lista di hotel aggiornata
     * @return un array che indica quali posizioni hanno subito un aggiornamento
     */
    final boolean[] rankingUpdate(String city, List<String> hotels) {
        boolean[] updated = new boolean[5];
        List<String> ranking = getRanking(city); // classifica non aggiornata
        // Aggiorno la classifica memorizzata con la nuova
        setRanking(city, hotels);
        // Per ogni posizione, controlla se è sempre presente lo stesso hotel o meno
        for (int i = 0; i < Hotel.Type.N_TYPES; i++) updated[i] = (!hotels.get(i).equals(ranking.get(i)));
        return updated;
    }


    public boolean register(String username, String password) {
        String new_password;
        if ((new_password = formatPassword(password)) == null) new_password = password;
        try {
            if (server.register(username, new_password)) {
                showMessageDialog(frame, "Registrazione completata con successo!",
                    "Registration success", INFORMATION_MESSAGE);
                return true;
            } else showMessageDialog(frame, "Si è verificato un errore durante la registrazione.\nRiprova.",
                "Registration error", WARNING_MESSAGE);
        } catch (InvalidPasswordException | UsernameAlreadyTakenException e) {
            errorDialog(e.getMessage());
        } catch (RemoteException e) {
            exitDialog("HOTELIER ha smesso di funzionare.");
        }
        return false;
    }

    public boolean login(String username, String password) {
        String new_password;
        if ((new_password = formatPassword(password)) == null) new_password = password;
        try {
            String[] reply = sendRequest(Request.getMessage(LOGIN, username, new_password));
            if (reply == null) return false;
            if (reply.length > 0) {
                if (reply[0].startsWith(SUCCESS.toString())) {
                    setSession(reply[1].split(":")[1]);
                    if (!multicast.isAlive()) multicast.start();
                    multicast.subscribe();
                    return true;
                } else errorDialog(reply[1]);
            } else showMessageDialog(frame, "Si è verificato un errore durante l'accesso.\nRiprova.",
                        "Login error", WARNING_MESSAGE);
        } catch (IOException e) {
            exitDialog("Si è verificato un errore durante l'accesso.");
        }
        return false;
    }

    public void registerForCallback(List<String> cities) {
        try {
            setLocalRanking(cities);
            List<List<String>> hotels = server.registerForCallback(stub, cities);
            if (hotels != null) {
                for (int i = 0; i < cities.size(); i++) setRanking(cities.get(i), hotels.get(i));
                showMessageDialog(frame, "Città per cui desideri ricevere aggiornamenti in questa sessione:\n"
                                + cities.toString().replaceAll("[\\[|\\]]", "") + ".",
                        "Ranking update notification", INFORMATION_MESSAGE);
            } else showMessageDialog(frame, "Si è verificato un errore durante l'impostazione delle notifiche.",
                        "Ranking update error", WARNING_MESSAGE);
        } catch (RemoteException e) {
            exitDialog("Si è verificato un errore durante l'accesso.");
        }
    }

    public boolean logout() {
        try {
            String[] reply = sendRequest(Message.Request.getMessage(LOGOUT, getSession()));
            if (reply == null) return false;
            if (reply.length > 0) {
                if (reply[0].startsWith(SUCCESS.toString())) {
                    multicast.unsubscribe();
                    server.unregisterForCallback(stub);
                    setSession(null);
                    setLocalRanking(null);
                    showMessageDialog(frame, "Sei stato disconnesso.", "Logout success", INFORMATION_MESSAGE);
                    return true;
                } else errorDialog(reply[1]);
            } else showMessageDialog(frame, "Si è verificato un errore durante il logout.\nRiprova.",
                    "Logout error", WARNING_MESSAGE);
        } catch (IOException e) {
            exitDialog("Si è verificato un errore durante il logout.");
        }
        return false;
    }


    public String[] search(String hotel, String city) {
        try {
            String[] reply;
            if (hotel == null) reply = sendRequest(Message.Request.getMessage(SEARCHALL, city));
            else reply = sendRequest(Message.Request.getMessage(SEARCH, hotel, city));
            if (reply == null) return null;
            if (reply.length > 0) {
                if (reply[0].startsWith(SUCCESS.toString())) {
                    if (!reply[0].split(" ", 2)[1].equals(NOTFOUND.info)) return reply[1].split("\n");
                    else return new String[]{};
                } else errorDialog(reply[1]);
            } else showMessageDialog(frame, "Si è verificato un errore durante la ricerca.\nRiprova.",
                    "Search error", WARNING_MESSAGE);
        } catch (IOException e) {
            exitDialog("Si è verificato un errore durante la ricerca.");
        }
        return null;
    }

    public void insertReview(String hotel, String city, double score, double[] scores) {
        try {
            String[] reply = sendRequest(Message.Request.getMessage(INSERTREVIEW, getSession(), hotel, city, score, scores));
            if (reply == null) return;
            if (reply.length > 0)
                if (reply[0].startsWith(SUCCESS.toString())) showMessageDialog(frame, "Recensione postata!",
                        "Insert review success", INFORMATION_MESSAGE);
                else errorDialog(reply[1]);
            else showMessageDialog(frame, "Si è verificato un errore la pubblicazione della recensione.\nRiprova.",
                    "Insert review error", WARNING_MESSAGE);
        } catch (IOException e) {
            exitDialog("Si è verificato un errore durante la pubblicazione della recensione.");
        }
    }

    public String[] showReviews(String hotel, String city) {
        try {
            String[] reply = sendRequest(Message.Request.getMessage(SHOWREVIEWS, hotel, city));
            if (reply == null) return null;
            if (reply.length > 0) {
                if (reply[0].startsWith(SUCCESS.toString())) {
                    if (!reply[0].split(" ", 2)[1].equals(NOTFOUND.info)) return reply[1].split("\n");
                    else return new String[]{};
                } else errorDialog(reply[1]);
            } else showMessageDialog(frame, "Si è verificato un errore durante la visualizzazione delle recensioni.\nRiprova.",
                    "Show reviews error", WARNING_MESSAGE);
        } catch (IOException e) {
            exitDialog("Si è verificato un errore durante la visualizzazione delle recensioni.");
        }
        return null;
    }

    public boolean upvote(String review) {
        try {
            String[] reply = sendRequest(Message.Request.getMessage(UPVOTE, review));
            if (reply == null) return false;
            if (reply.length > 0) {
                if (reply[0].startsWith(SUCCESS.toString())) {
                    if (!reply[0].split(" ")[1].equals(FAILURE.info)) return true;
                    else showMessageDialog(frame, "Hai già registrato il tuo voto per questa recensione!",
                            "Upvote review info", WARNING_MESSAGE);
                } else errorDialog(reply[1]);
            } else  showMessageDialog(frame, "Si è verificato un errore durante la registrazione del voto.\nRiprova.",
                    "Upvote review error", WARNING_MESSAGE);
        } catch (IOException e) {
            exitDialog("Si è verificato un errore durante la registrazione del voto.");
        }
        return false;
    }

    public String[] showMyReviews() {
        try {
            String[] reply = sendRequest(Message.Request.getMessage(SHOWMYREVIEWS, getSession()));
            if (reply == null) return null;
            if (reply.length > 0) {
                if (reply[0].startsWith(SUCCESS.toString())) {
                    if (!reply[0].split(" ", 2)[1].equals(NOTFOUND.info)) return reply[1].split("\n");
                    else return new String[]{};
                } else errorDialog(reply[1]);
            } else showMessageDialog(frame, "Si è verificato un errore durante la visualizzazione delle tue recensioni.\nRiprova.",
                    "Show reviews error", WARNING_MESSAGE);
        } catch (IOException e) {
            exitDialog("Si è verificato un errore durante la visualizzazione delle tue recensioni.");
        }
        return null;
    }

    public String showMyBadges() {
        try {
            String[] reply = sendRequest(Message.Request.getMessage(SHOWMYBADGES, getSession()));
            if (reply == null) return null;
            if (reply.length > 0) {
                if (reply[0].startsWith(SUCCESS.toString())) return reply[1];
                else errorDialog(reply[1]);
            } else showMessageDialog(frame, "Si è verificato un errore durante la visualizzazione del distintivo.\nRiprova.",
                    "Show badges error", WARNING_MESSAGE);
        } catch (IOException e) {
            exitDialog("Si è verificato un errore durante la visualizzazione del distintivo.");
        }
        return null;
    }


    void exitDialog(String message) {
        showMessageDialog(frame, message + "\nImpossibile contattare il server. L'applicazione verrà chiusa.",
                "Error message", ERROR_MESSAGE);
        System.exit(1);
    }

    void errorDialog(String error) {
        showMessageDialog(frame, error, "Error message", ERROR_MESSAGE);
    }
}