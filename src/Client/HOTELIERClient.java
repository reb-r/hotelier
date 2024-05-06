package Client;

import Server.RMIHOTELIERServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteObject;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * <p align="justify">
 *     La classe astratta {@code HOTELIERClient} estende la classe astratta {@link RemoteObject} e implementa l'interfaccia
 *     {@link CallbackHOTELIERClient}.
 * </p> <p align="justify">
 *     Si occupa dell'interazione tra l'utente che fa uso del servizio e il server.
 * </p>
 */
public abstract class HOTELIERClient extends RemoteObject implements Runnable, CallbackHOTELIERClient {
    /** Codifica di caratteri in uso */
    public static final Charset CHARSET = StandardCharsets.UTF_8;
    /** Dimensione del buffer */
    private static final int capacity = 1024;

    /** Canale per operazioni di I/O sulla rete via TCP */
    protected final SocketChannel channel;
    /** Istanza del server RMI per i servizi remoti */
    protected final RMIHOTELIERServer server;
    /** Riferimento a questo oggetto remoto */
    protected final CallbackHOTELIERClient stub;
    /** Istanza del client che si occupa della ricezione dei messaggi multicast */
    protected MulticastClient multicast;
    /** Buffer per i messaggi */
    protected final ByteBuffer buffer = ByteBuffer.allocate(capacity);
    /** Sessione attiva */
    protected String session; // null se in modalità guest o nome utente dell'utente loggato
    /** Struttura dati per le classifiche locali */
    protected Map<String, List<String>> localRanking;
    // lista di coppie <città d'interesse, lista di hotel in ordine di ranking> aggiornata periodicamente dal server


    public HOTELIERClient(String server_address, int connection_port, int registry_port) throws IOException, NotBoundException {
        // Assegno i parametri alle variabili
        // Apro la socket channel per comunicare con il server alla porta indicata
        channel = SocketChannel.open();
        channel.connect(new InetSocketAddress(server_address, connection_port));
        // Reperisco dal registry lo stub associato al servizio remoto
        Registry registry = LocateRegistry.getRegistry(server_address, registry_port);
        server = (RMIHOTELIERServer) registry.lookup(RMIHOTELIERServer.SERVICE_NAME);
        // Esporto questo oggetto remoto per poter usare le callback
        stub = (CallbackHOTELIERClient) UnicastRemoteObject.exportObject(this, 0);
        session = null;
    }

    @Override
    public abstract void run();


    @Override
    public abstract void notifyEvent(String city, List<String> hotels) throws RemoteException;


    // Metodi get e set
    String getSession() { return session; }

    void setSession(String user) { session = user; }

    List<String> getRanking(String city) { return new ArrayList<>(localRanking.get(city)); }

    /**
     * <p align="justify">
     *     Se il parametro è {@code null} rimuove dalla struttura dati che gestisce i rankings tutte le associazioni
     *     tra città e hotel effettuate in precedenza, altrimenti vi inserisce le città indicate.
     * </p>
     * @param cities la lista di città per cui si intende ricevere le notifiche relative all'aggiornamento delle
     *               classifiche o {@code null} per rimuovere le associazioni
     */
    synchronized void setLocalRanking(List<String> cities) {
        // Se la lista è null, rimuovo tutti i mapping in modo da poterne creare di nuovi al prossimo accesso
        // Altrimenti inizializzo la map con la lista di città
        if (cities != null) localRanking = cities.stream().collect(Collectors.toMap(item -> item, item -> new LinkedList<>()));
        else if (localRanking != null) localRanking.clear();
    }

    /**
     * <p align="justify">
     *     Assegna alla città indicata all'interno della struttura dati che gestisce i rankings la lista di hotel.
     * </p>
     * @param city la città di interesse, selezionata in precedenza
     * @param hotels la lista di hotel presenti nella città
     */
    synchronized void setRanking(String city, List<String> hotels) {
        localRanking.get(city).addAll(hotels);
    }


    /**
     * <p align="justify">
     *     Invia una richiesta sul canale al server e attende una sua risposta.
     *     Una volta ottenuta la risposta, questa viene suddivisa in sotto-stringhe, così da essere pronta per il parsing.
     * </p> <p align="justify">
     *     Se la connessione è stata interrotta, il canale viene chiuso e restituisce immediatamente {@code null}.
     * </p>
     * @param message il messaggio di richiesta inviato da questo client
     * @return il messaggio di risposta ricevuto dal server o {@code null}
     */
    final String[] sendRequest(String message) throws IOException {
        // Controllo se il canale è connesso prima di inviare la richiesta, altrimenti ritorno
        if (!channel.isConnected()) return null;
        else channel.write(ByteBuffer.wrap(message.getBytes(CHARSET)));

        StringBuilder msg = new StringBuilder();
        // Finché il buffer viene riempito interamente, continuo a leggere dal canale per verificare che non ci sia altro
        while (buffer.limit() == buffer.capacity()) {
            buffer.clear(); // svuoto il buffer e torno in modalità scrittura
            channel.read(buffer); // leggo i dati presenti sul canale nel buffer
            buffer.flip(); // torno in modalità lettura
            byte[] bytes = new byte[buffer.limit()];
            buffer.get(bytes); // copio i dati dal buffer in un array di bytes
            msg.append(new String(bytes)); // appendo la nuova stringa alla stringa costruita
        }
        buffer.clear(); // svuoto il buffer e torno in modalità scrittura per la prossima ricezione (limit = capacity)
        return msg.toString().split("\n", 2);
    }


    /**
     * <p align="justify">
     *     Effettua l'hash della password per non trasmetterla sul canale e salvarla nel database sul server in chiaro.
     *     Utilizza la tecnica MD5 (Message Digest).
     * </p>
     * @param password la password di cui effettuare l'hashing
     * @return una stringa che rappresenta l'hash della {@code password}
     */
    public static String formatPassword(String password) {
        try {
            MessageDigest msg = MessageDigest.getInstance("MD5");
            msg.update(password.getBytes());
            byte[] bytes = msg.digest();
            StringBuilder new_password = new StringBuilder();
            for (byte aByte: bytes)
                new_password.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            return new_password.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }


    // Altri metodi
    /**
     * <p align="justify">
     *     Determina se un utente ha effettuato il login.
     * </p>
     * @return {@code true} se è loggato un utente, {@code false} altrimenti
     */
    boolean isLoggedIn() { return (session != null); }


    /**
     * <p align="justify">
     *     La classe {@code Command} rappresenta il comando che viene invocato dall'utente sul client in esecuzione.
     * </p>
     * @see #REGISTER
     * @see #LOGIN
     * @see #LOGOUT
     * @see #SEARCH
     * @see #SEARCHALL
     * @see #INSERTREVIEW
     * @see #SHOWMYBADGES
     * @see #SHOWREVIEWS
     * @see #SHOWMYREVIEWS
     * @see #UPVOTE
     * @see #HELP
     * @see #CANCEL
     * @see #QUIT
     */
    public enum Command {
        /** Comando per la registrazione */
        REGISTER("register", "per registrarsi", "username, password"),
        /** Comando per il login */
        LOGIN("login", "per effettuare l'accesso", "username, password"),
        /** Comando per il logout */
        LOGOUT("logout", "per disconnettersi", "username"),
        /** Comando per la ricerca di un hotel */
        SEARCH("search", "per effettuare una ricerca di un hotel in una città", "hotel, città"),
        /** Comando per la ricerca degli hotel per città */
        SEARCHALL("searchall", "per trovare tutti gli hotel di una città", "città"),
        /** Comando per la pubblicazione di una recensione */
        INSERTREVIEW("insertreview", "per pubblicare una recensione riferita a un hotel di una città",
                "hotel, città, punteggio complessivo, punteggi per categoria"),
        /** Comando per la per la visualizzazione dei badges ottenuti */
        SHOWMYBADGES("showmybadges", "per visualizzare il tuo badge", "none"),
        /** Comando per la visualizzazione delle recensioni di un hotel */
        SHOWREVIEWS("showreviews", "per visualizzare tutte le recensioni riferite all'hotel di una città",
                "hotel, città"),
        /** Comando per la votazione di una recensione */
        UPVOTE("upvote", "per consigliare una recensione", "#recensione"),
        /** Comando per la visualizzazione delle recensioni pubblicate */
        SHOWMYREVIEWS("showmyreviews", "per visualizzare le recensioni che hai pubblicato", "none"),
        /** Comando per ricevere aiuto */
        HELP("help", " l'o", null),
        /** Comando per annullare */
        CANCEL("cancel", "per annullare un'operazione in qualsiasi momento", null),
        /** Comando per uscire dall'applicazione */
        QUIT("quit", "per uscire dall'applicazione", null);

        /** Comando */
        final String command;
        /** Descrizione del comando */
        final String description;
        /** Parametri richiesti dal comando */
        final String parameters;

        Command(String command, String description, String parameters) {
            this.command = command;
            this.description = description;
            this.parameters = parameters;
        }

        /**
         * <p align="justify">
         *     Effettua il parsing della stringa indicata, restituendo il comando corrispondente.
         * </p>
         * @param constant la stringa di cui effettuare il parsing
         * @return il comando corrispondente
         */
        public static Command parseCommand(String constant) {
            for (Command command: Command.values()) if (command.command.equals(constant)) return command;
            return null;
        }
    }
}