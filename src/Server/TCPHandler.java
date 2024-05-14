package Server;

import MyExceptions.*;
import Server.Database.Hotel.*;
import Server.Message.Request.Method;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;

import static Server.Message.Reply.Session.*;
import static Server.Message.Reply.Error.*;
import static Server.Message.Reply.Info.*;
import static Server.Message.Reply.Status.*;
import static Server.Message.Request.Method.*;


/**
 * <p align="justify">
 *     La classe {@code TCPHandler} estende la classe {@link Thread} e si occupa della gestione delle connessioni TCP
 *     con i client che richiedono i servizi offerti dal server.
 * </p>
 */
public class TCPHandler extends Thread {
    /** Porta di ascolto per la richiesta di connessioni */
    public static int listening_port;
    /** Codifica di caratteri in uso */
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    /** Dimensione del buffer */
    private static final int capacity = 1024; // costante

    /** Canale di ascolto per richieste di connessioni TCP */
    private final ServerSocketChannel serverChannel;
    /** Selettore per la gestione delle client sockets */
    private final Selector selector;
    /** Istanza del server per l'elaborazione delle richieste */
    private final HOTELIERServer server;
    /** Buffer per la gestione dei messaggi */
    private ByteBuffer buffer = ByteBuffer.allocate(capacity);

    public TCPHandler(HOTELIERServer server, int listening_port) throws IOException {
        // Assegno i parametri alle variabili
        TCPHandler.listening_port = listening_port;
        this.server = server;
        // Apro la server socket channel alla listening port
        this.serverChannel = ServerSocketChannel.open();
        this.serverChannel.bind(new InetSocketAddress(listening_port));
        // Definisco il comportamento del canale come non bloccante per poter registrare questo canale con il selettore
        this.serverChannel.configureBlocking(false);
        // Apro il selettore e registro la server socket channel appena definita con operazione di interesse OP_ACCEPT
        this.selector = Selector.open();
        this.serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println(HOTELIERServer.printCurrentDate() + "\tserver is listening for connections on port " + listening_port);
    }

    @Override
    public void run() {
        while (serverChannel.isOpen()) {
            try {
                if (selector.select() == 0) continue; // se non ci sono canali pronti, passo alla prossima iterazione
                // Reperisco l'insieme delle chiavi dei canali registrati pronti per una delle operazioni di I/O
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                // Itero sull'insieme di chiavi, per determinare l'operazione per cui il canale è pronto
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();
                    try {
                        // Controllo per quale operazione il canale associato alla chiave è pronto
                        if (key.isAcceptable()) handleAccept(key); // OP_ACCEPT
                        if (key.isReadable()) handleRead(key); // OP_READ
                        if (key.isWritable()) handleWrite(key); // OP_WRITE
                    } catch (IOException e) {
                        String atch = new String(((ByteBuffer) key.attachment()).array());
                        String client = ((InetSocketAddress) ((SocketChannel) key.channel()).getRemoteAddress()).getHostString();
                        System.out.println(HOTELIERServer.printCurrentDate() + "\tclient "+ client + " closed connection");
                        // Se il client ha chiuso la connessione senza fare il logout, viene fatto in automatico
                        if (atch.startsWith(SESSION.session)) {
                            try {
                                server.logout(atch.split(" ")[0].split(":")[1]);
                                System.out.println(HOTELIERServer.printCurrentDate()
                                        + "\t" + atch.split(" ")[0].split(":")[1] + " left");
                            } catch (UserNotRegisteredException | UserNotLoggedInException ignored) { }
                        }
                        key.channel().close(); // chiudo il canale
                        System.out.println(HOTELIERServer.printCurrentDate() + "\tconnection with client " + client + " closed");
                        key.cancel(); // cancello la registrazione di questo canale
                    }
                }
            } catch (IOException e) {
                System.out.println(HOTELIERServer.printCurrentDate()
                        + "\tServer.TCPHandler.run(): an error occurred while handling connections");
            }
        }
    }


    // Metodi per la gestione delle operazioni sui canali pronti
    /**
     * <p align="justify">
     *     Accetta le nuove connessioni in entrata.
     * </p> <p align="justify">
     *     Registra i canali al selettore con operazione di interesse {@link SelectionKey#OP_READ}.
     * </p>
     * @param key la chiave di selezione che include il riferimento al canale pronto e registrato con il selettore
     * @throws IOException
     */
    private void handleAccept(SelectionKey key) throws IOException {
        // Reperisco il canale associato alla chiave indicata e accetto la connessione col client
        SocketChannel client = ((ServerSocketChannel) key.channel()).accept();
        InetSocketAddress socketAddress = ((InetSocketAddress) client.getRemoteAddress());
        System.out.println(HOTELIERServer.printCurrentDate() + "\tserver accepted connection with client "
                + socketAddress.getHostString() + " on port " + socketAddress.getPort());
        client.configureBlocking(false);
        // Registro il canale al selettore, con operazione di interesse OP_READ e con come attachment un buffer
        // Il buffer dell'attachment contiene i messaggi appena inviati o da inviare dal server al client
        // in cui il primo elemento è un indicatore sul tipo sessione: utente loggato o guest
        client.register(selector, SelectionKey.OP_READ,
                ByteBuffer.wrap((GUEST + socketAddress.getHostString()).getBytes(CHARSET)));
    }

    /**
     * <p align="justify">
     *     Legge i dati presenti sul canale nel buffer ed elabora la richiesta.
     * </p> <p align="justify">
     *     Al termine, registra il canale con operazione di interesse {@link SelectionKey#OP_WRITE}.
     * </p>
     * @param key la chiave di selezione che include il riferimento al canale pronto e registrato con il selettore
     * @throws IOException
     */
    private void handleRead(SelectionKey key) throws IOException {
        // Reperisco il canale associato alla chiave indicata
        SocketChannel channel = (SocketChannel) key.channel();
        buffer.clear(); // svuoto il buffer e torno in modalità scrittura
        int bytesRead;
        StringBuilder request = new StringBuilder(); // per costruire la risposta
        // Leggo i dati presenti sul canale nel buffer, controllando che effettivamente sia stato inviato qualcosa
        while ((bytesRead = channel.read(buffer)) > 0) {
            buffer.flip(); // torno in modalità lettura
            byte[] bytes = new byte[buffer.limit()];
            buffer.get(bytes); // copio i dati dal buffer in un array di bytes
            request.append(new String(bytes)); // appendo la nuova stringa alla stringa costruita
            buffer.clear(); // svuoto il buffer e torno in modalità scrittura
        }
        // Controllo che non sia stata raggiunta la fine dello stream, nel caso chiudo la connessione
        if (bytesRead < 0) channel.close();
        else {
            String message = request.toString(); // messaggio di richiesta
            String attachment = new String(((ByteBuffer) key.attachment()).array()); // attachment registrato con il canale

            // Divido l'attachment in due parti per prelevare il primo elemento contenente il tipo di sessione in uso
            String session = attachment.split(" ", 2)[0];
            // Divido il messaggio di richiesta in due parti
            // La prima parte contiene il servizio richiesto dal client
            Method method = fetchMethod(message.split(" \"?", 2)[0]);
            // La seconda parte contiene i parametri per poter elaborare la richiesta
            // (viene divisa ulteriormente a ogni spazio non contenuto tra due '"'
            String[] msgList = message.split(" \"?", 2)[1].split("\"?( |$)(?=(([^\"]*\"){2})*[^\"]*$)\"?");
            // Se il messaggio dopo il servizio richiesto contiene l'identificatore USER, lo assegno alla variabile
            String user = (msgList[0].startsWith(USER.session))? msgList[0].split(":")[1] : "";
            message = session + " "; // inizializzo il messaggio di risposta con la sessione

            // Determino la richiesta da parte del client
            // Nel caso in cui non ci sia stato riscontro per il metodo, la richiesta non è conforme
            if (method == null) message += ERROR + BADREQUESTERROR.toString();
            else switch (method) {
                case LOGIN:
                    // Controllo che sul client sia attiva una sessione da guest e faccio il login,
                    // altrimenti comunico che c'è stato un errore
                    if (session.startsWith(GUEST.session)) {
                        try {
                            // Comunico l'esito del login
                            if (server.login(msgList[0], msgList[1])) message = SESSION + msgList[0] + " " + SUCCESS
                                    + OK.info + "\n" + USER + msgList[0];
                            else message += ERROR + LOGINERROR.toString();
                        } catch (UserNotRegisteredException | UserAlreadyLoggedInException
                                 | InvalidPasswordException | WrongPasswordException e) {
                            message += ERROR + exceptionToString(e);
                        }
                    } else message += ERROR + SESSIONERROR.toString();
                    break;
                case LOGOUT:
                    // Controllo che sul client sia attiva una sessione con un utente loggato e provo a fare il logout,
                    // altrimenti comunico che c'è stato un errore
                    if (session.startsWith(SESSION.session)) {
                        // Controllo che l'utente della sessione attiva e l'utente che fa richiesta di logout
                        // siano effettivamente lo stesso, altrimenti comunico l'errore
                        if (session.split(":")[1].equals(user)) {
                            try {
                                String client = ((InetSocketAddress)
                                        ((SocketChannel) key.channel()).getRemoteAddress()).getHostString();
                                // Comunico l'esito del logout
                                if (server.logout(user)) message = GUEST + client + " " + SUCCESS + DONE.info;
                                else message += ERROR + LOGOUTERROR.toString();
                            } catch (UserNotLoggedInException | UserNotRegisteredException e) {
                                message += ERROR + exceptionToString(e);
                            }
                        } else message += ERROR + SESSIONERROR.toString();
                    } else message += ERROR + SESSIONERROR.toString();
                    break;
                case SEARCH:
                    try {
                        // Effettuo la ricerca e controllo che non ci siano stati errori, altrimenti lo comunico
                        String results = server.searchHotel(msgList[0], msgList[1]);
                        if (results != null) {
                            // Comunico l'esito della ricerca con il risultato ottenuto dall'esecuzione della stessa
                            if (!results.isEmpty()) message += SUCCESS + FOUND.info + "\n" + results;
                            else message += SUCCESS + NOTFOUND.info;
                        } else message += ERROR + SEARCHERROR.toString();
                    } catch (InvalidCityException e) {
                        message += ERROR + exceptionToString(e);
                    }
                    break;
                case SEARCHALL:
                    // Analogo alla SEARCH
                    try {
                        String results = server.searchAllHotels(msgList[0]);
                        if (results != null) {
                            if (!results.isEmpty()) message += SUCCESS + FOUND.info + "\n" + results;
                            else message += SUCCESS + NOTFOUND.info;
                        } else message += ERROR + SEARCHALLERROR.toString();
                    } catch (InvalidCityException e) {
                        message += ERROR + exceptionToString(e);
                    }
                    break;
                case INSERTREVIEW:
                    // Controllo che sul client sia attiva una sessione con un utente loggato
                    // e provo a pubblicare la recensione, altrimenti comunico l'errore
                    if (session.startsWith(SESSION.session)) {
                        // Controllo che l'utente della sessione attiva e l'utente che fa la richiesta
                        // siano effettivamente lo stesso, altrimenti comunico l'errore
                        if (session.split(":")[1].trim().equals(user)) {
                            try {
                                // Definisco i parametri necessari per pubblicare la recensione
                                double[] scores = new double[Ratings.getnCategories()];
                                for (int i = 0; i < scores.length; i++) scores[i] = Double.parseDouble(msgList[i + 4]);
                                // Pubblico la recensione
                                server.insertReview(msgList[1], msgList[2], Double.parseDouble(msgList[3]), scores, user);
                                // Comunico l'esito della pubblicazione
                                message += SUCCESS + DONE.info;
                            } catch (UserNotLoggedInException | UserNotRegisteredException | InvalidHotelException
                                     | InvalidCityException | InvalidScoreException | ReviewAlreadyPostedException e) {
                                message += ERROR + exceptionToString(e);
                            }
                        } else message += ERROR + SESSIONERROR.toString();
                    } else message += ERROR + SESSIONERROR.toString();
                    break;
                case SHOWREVIEWS:
                    try {
                        // Reperisco tutte le recensioni relative all'hotel indicato dalla richiesta
                        // e controllo che non ci siano stati errori, altrimenti lo comunico
                        String results = server.showAllReviews(msgList[0], msgList[1]);
                        if (results != null) {
                            // Comunico l'esito dell'operazione
                            if (!results.isEmpty()) message += SUCCESS + FOUND.info + "\n" + results;
                            else message += SUCCESS + NOTFOUND.info;
                        } else message += ERROR + SHOWREVIEWSERROR.toString();
                    } catch (InvalidHotelException | InvalidCityException e) {
                        message += ERROR + exceptionToString(e);
                    }
                    break;
                case UPVOTE:
                    try {
                        // Comunico l'esito dell'operazione
                        if (server.upvote(msgList[0].replace("#", ""), session.split(":", 2)[1]))
                            message += SUCCESS + DONE.info;
                        else message += SUCCESS + FAILURE.info;
                    } catch (InvalidReviewException | InvalidVoteException e) {
                        message += ERROR + exceptionToString(e);
                    }
                    break;
                case SHOWMYREVIEWS:
                    // Controllo che sul client sia attiva una sessione con un utente loggato
                    // e provo a elaborare la richiesta, altrimenti comunico che c'è stato un errore
                    if (session.startsWith(SESSION.session)) {
                        // Controllo che l'utente della sessione attiva e l'utente che fa la richiesta
                        // siano effettivamente lo stesso, altrimenti comunico l'errore
                        if (session.split(":")[1].equals(user)) {
                            try {
                                // Reperisco tutte le recensioni pubblicate dall'utente che ha fatto la richiesta
                                String results = server.showMyReviews(user);
                                if (results != null) {
                                    // Comunico l'esito dell'operazione
                                    if (!results.isEmpty()) message += SUCCESS + FOUND.info + "\n" + results;
                                    else message += SUCCESS + NOTFOUND.info;
                                } else message += ERROR + SHOWMYREVIEWSERROR.toString();
                            } catch (UserNotRegisteredException | UserNotLoggedInException e) {
                                message += ERROR + exceptionToString(e);
                            }
                        } else message += ERROR + SESSIONERROR.toString();
                    } else message += ERROR + SESSIONERROR.toString();
                    break;
                case SHOWMYBADGES:
                    // Controllo che sul client sia attiva una sessione con un utente loggato
                    // e provo a elaborare la richiesta, altrimenti comunico che c'è stato un errore
                    if (session.startsWith(SESSION.session)) {
                        // Controllo che l'utente della sessione attiva e l'utente che fa la richiesta
                        // siano effettivamente lo stesso, altrimenti comunico l'errore
                        if (session.split(":")[1].equals(user)) {
                            try {
                                // Comunico l'esito dell'operazione
                                String badge = server.showMyBadges(user);
                                if (badge != null) message += SUCCESS + MORE.info + "\n"
                                        + ((badge.isBlank())? "N/A" : badge);
                                else message += ERROR + SHOWMYBADGESERROR.toString();
                            } catch (UserNotRegisteredException | UserNotLoggedInException e) {
                                message += ERROR + exceptionToString(e);
                            }
                        } else message += ERROR + SESSIONERROR.toString();
                    } else message += ERROR + SESSIONERROR.toString();
            }

            // Registro il canale con operazione di interesse OP_WRITE in modo da renderlo disponibile
            // per una scrittura sullo stesso e inviare la risposta al client
            channel.register(selector, SelectionKey.OP_WRITE, ByteBuffer.wrap(message.getBytes()));
        }
    }

    /**
     * <p align="justify">
     *     Scrive i dati dal buffer sul canale.
     * </p> <p align="justify">
     *     Registra il canale con operazione di interesse {@link SelectionKey#OP_READ}.
     * </p>
     * @param key la chiave di selezione che include il riferimento al canale pronto e registrato con il selettore
     * @throws IOException
     */
    private void handleWrite(SelectionKey key) throws IOException {
        // Reperisco il canale associato alla chiave indicata e l'attachment
        SocketChannel channel = (SocketChannel) key.channel();
        buffer = (ByteBuffer) key.attachment();
        String reply = new String (buffer.array());
        // Rimuovo dalla risposta il primo elemento che fa riferimento al tipo di sessione del client
        buffer = ByteBuffer.wrap(reply.split(" ", 2)[1].getBytes(CHARSET));
        channel.write(buffer); // scrivo il messaggio sul canale
        // Quando ho scritto tutto, registro di nuovo il canale con operazione di interesse OP_READ
        // in modo da renderlo disponibile per un'eventuale parsing di una nuova richiesta
        if (buffer.hasRemaining()) return;
        buffer.clear();
        channel.register(selector, SelectionKey.OP_READ, key.attachment());
    }


    // Altri metodi
    /**
     * <p align="justify">
     *     Restituisce il nome semplice dell'eccezione indicata, seguita dalla descrizione della stessa.
     * </p>
     * @param e l'eccezione
     * @return una string che rappresenta le informazioni relative all'eccezione
     */
    public String exceptionToString(Exception e) { return e.getClass().getSimpleName() + "\n" + e.getMessage(); }
}