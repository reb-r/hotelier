package Server;

import MyExceptions.*;
import Server.Database.*;
import Server.Database.Hotel.*;
import Client.CallbackHOTELIERClient;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * <p align="justify">
 *     La classe {@code HOTELIERServer} estende la classe astratta {@link RemoteServer} e implementa le interfacce
 *     {@link RMIHOTELIERServer} e {@link HOTELIERServerInterface}.
 * </p> <p align="justify">
 *     Si occupa della gestione delle strutture dati del servizio e implementa i metodi principali dichiarati dalle
 *     interfacce e altri aggiuntivi.
 * </p>
 */
public class HOTELIERServer extends RemoteServer implements RMIHOTELIERServer, HOTELIERServerInterface {
    private static int N_USERS = 0; // record di tipo User
    private static int N_REVIEWS = 0; // record di tipo Review

    /** Struttura dati per gli utenti */
    private final Map<String, User> users; // lista di coppie <nome utente, utente>
    /** Struttura dati per gli hotels */
    private final Map<String, Hotel> hotels; // lista di coppie <nome hotel, hotel>
    /** Struttura dati per le recensioni in base all'hotel */
    private final Map<String, LinkedList<Review>> reviews; // lista di coppie <nome hotel, lista di recensioni>
    /** Struttura dati per le classifiche locali di hotels per città */
    private final Map<String, List<Hotel>> localRanking; // lista di coppie <nome città, lista di hotel>
    /** Struttura dati per i client registrati al servizio di notifica per le città di interesse */
    private final Map<CallbackHOTELIERClient, List<String>> clients;
    // lista di coppie <client registrato per callback, città di interesse>
    /** Utenti loggati/online */
    private final List<String> onlineUsers; // lista degli utenti che hanno effettuato l'accesso

    public HOTELIERServer(int connection_port, int registry_port, String hotel_filename, String user_filename,
                          String file_format, String config_filepath, String backup_filepath,
                          long ranking_timeout, long backup_timeout, String multicast_address, int multicast_port)
            throws IOException, AlreadyBoundException {
        // Inizializzo le strutture dati
        this.onlineUsers = Collections.synchronizedList(new LinkedList<>());
        this.clients = Collections.synchronizedMap(new HashMap<>());
        this.users = Collections.synchronizedMap(new HashMap<>());
        this.hotels = Collections.synchronizedMap(new HashMap<>());
        this.reviews = Collections.synchronizedMap(new HashMap<>());
        this.localRanking = Collections.synchronizedMap(new HashMap<>());
        for (City city: City.values()) localRanking.put(city.getName(), new LinkedList<>()); // le città sono le entries della map

        // Controllo se le directory per il backup esistono, altrimenti le creo
        File hotel_backup = new File(backup_filepath + BackupHandler.hotel_dir);
        File user_backup = new File(backup_filepath + BackupHandler.user_dir);
        if (!hotel_backup.exists()) hotel_backup.mkdirs();
        if (!user_backup.exists()) user_backup.mkdirs();
        // Reperisco le liste di file contenuti nelle directory
        File[] hotel_folder = hotel_backup.listFiles();
        File[] user_folder = user_backup.listFiles();
        // Per i file relativi ai database, inizializzo le variabili con i file contenuti nella directory Server/config/
        File user_database = new File(config_filepath + user_filename + file_format);
        File hotel_database = new File(config_filepath + hotel_filename + file_format);

        if (hotel_folder != null) for (File file: hotel_folder) {
            // Controllo che il file non sia vuoto, altrimenti proseguo
            if (file.length() == 0) continue;
            // Controllo se è un file di backup di hotels e che la data di ultima modifica sia più recente di quella del file corrente
            // In caso positivo, assegno alla variabile il file appena controllato
            if (file.getName().contains(hotel_filename) && file.getName().contains(file_format))
                if (file.lastModified() > hotel_database.lastModified()) hotel_database = file;
        } if (user_folder != null) for (File file: user_folder) { // come sopra
            if (file.length() == 0) continue;
            if (file.getName().contains(user_filename) && file.getName().contains(file_format))
                if (file.lastModified() > user_database.lastModified()) user_database = file;
        }

        // Recupero i backup per ripristinare le strutture dati
        readFile(hotel_database, user_database);

        // Inizializzo e avvio i threads che si occupano dell'aggiornamento dei ranking locali, dei backup
        // e della gestione delle connessioni/richieste
        RankingHandler rankingHandler = new RankingHandler(ranking_timeout, this, multicast_address, multicast_port);
        rankingHandler.start();
        BackupHandler backupHandler = new BackupHandler(backup_timeout, this, user_filename, hotel_filename,
                file_format, backup_filepath);
        backupHandler.start();
        TCPHandler TCPHandler = new TCPHandler(this, connection_port);
        TCPHandler.start();

        // Esporto questo oggetto remoto e lo registro nel registry appena definito, creando il collegamento tra questo
        // e il nome simbolico
        RMIHOTELIERServer stub = (RMIHOTELIERServer) UnicastRemoteObject.exportObject(this, 0); // porta anonima
        LocateRegistry.createRegistry(registry_port);
        LocateRegistry.getRegistry(registry_port).bind(RMIHOTELIERServer.SERVICE_NAME, stub);
    }

    /**
     * <p align="justify">
     *     Legge i file indicati e li memorizza nelle strutture dati corrispondenti.
     * </p>
     * @param hotel_database il file di backup degli hotel del servizio
     * @param user_database il file di backup degli utenti registrati
     */
    private void readFile(File hotel_database, File user_database) {
        // Definisco l'object mapper per deserializzare gli oggetti
        ObjectMapper objectMapper = new ObjectMapper();
        // Imposto le proprietà dell'object mapper
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        JsonFactory factory = new JsonFactory();
        JsonParser parser;

        // Leggo il file di backup degli hotel
        try {
            parser = factory.createParser(hotel_database);
            parser.setCodec(objectMapper);
            if (parser.nextToken() != JsonToken.START_ARRAY)
                System.out.println(printCurrentDate()
                        + "\tServer.HOTERLIERServerDatabase.readFile(): an error occurred while reading hotel config file");
            while (parser.nextToken() == JsonToken.START_OBJECT) {
                // Converto i dati JSON nell'oggetto di tipo Hotel
                Hotel hotel = parser.readValueAs(Hotel.class);
                // Aggiungo l'hotel decodificato alle strutture dati e definendo le key e le entries delle map
                hotels.put(hotel.getName(), hotel);
                reviews.put(hotel.getName(), new LinkedList<>());
                localRanking.get(hotel.getCity()).add(hotel);
            }
        } catch (IOException e) {
            System.out.println(printCurrentDate()
                    + "\tServer.HOTERLIERServerDatabase.readFile(): an error occurred with hotel file");
        }

        // Leggo il file di backup degli utenti
        try {
            parser = factory.createParser(user_database);
            parser.setCodec(objectMapper);
            if (parser.nextToken() != JsonToken.START_ARRAY)
                System.out.println(printCurrentDate()
                        + "\tServer.HOTERLIERServerDatabase.readFile(): an error occurred while reading user config file");
            while (parser.nextToken() == JsonToken.START_OBJECT) {
                // Converto i dati JSON nell'oggetto di tipo User
                User user = parser.readValueAs(User.class);
                // Aggiungo l'utente decodificato e le recensioni da questo pubblicate alle strutture dati
                users.put(user.getUsername(), user);
                for (Review review: user.getReviews()) reviews.get(review.getHotel()).add(review);
                N_USERS++;
                N_REVIEWS += user.getReviews().size();
            }
            // Per ogni hotel, una volta aggiunte tutte le recensioni, le riordino in ordine decrescente di data
            for (List<Review> reviews: reviews.values())
                reviews.sort(Comparator.comparingLong(Review::getTime).reversed());
        } catch (IOException e) {
            System.out.println(printCurrentDate()
                    + "\tServer.HOTERLIERServerDatabase.readFile(): an error occurred with user file");
            return;
        }

        System.out.println(printCurrentDate() + "\tconfig files have been successfully uploaded");
    }

    // Metodi set
    public static int setnUsers() { return ++N_USERS; }

    public static int setnReviews() { return ++N_REVIEWS; }


    // Metodi dichiari nell'interfaccia RMIHOTELIERServer
    @Override
    public synchronized boolean register(String username, String password)
            throws UsernameAlreadyTakenException,InvalidPasswordException {
        if (password == null) throw new InvalidPasswordException();
        if (users.containsKey(username)) throw new UsernameAlreadyTakenException();
        User user = new User(username, password);
        users.put(username, user);
        System.out.println(printCurrentDate() + "\tuser #" + user.getId() + " has been successfully registered");
        return true;
    }

    @Override
    public synchronized List<List<String>> registerForCallback(CallbackHOTELIERClient stub, List<String> cities)
            throws RemoteException {
        if (!clients.containsKey(stub)) {
            clients.put(stub, cities);
            System.out.println(printCurrentDate() + "\tnew client registered for callback");
        } else System.out.println(printCurrentDate()
                + "\tHOTELIERServer.registerForCallback(..): unable to register client for callback");
        if (cities != null) {
            // Per ogni città, individuo gli hotel presenti nella stessa e li memorizzo nella lista (come lista)
            List<List<String>> hotels = new LinkedList<>();
            for (String city: cities) hotels.add(new ArrayList<>(localRanking.get(city).stream().map(Hotel::getName).toList()));
            return hotels;
        } else return null;
    }

    @Override
    public synchronized void unregisterForCallback(CallbackHOTELIERClient stub) throws RemoteException {
        if (clients.containsKey(stub)) {
            clients.remove(stub);
            System.out.println(printCurrentDate() + "\tclient unregistered successfully");
        }
    }

    /**
     * <p align="justify">
     *     Notifica l'aggiornamento della classifica della città indicata ed effettua la callback a tutti i client
     *     registrati al servizio di notifica per gli aggiornamenti della città.
     * </p>
     * @param city la città per cui è stata aggiornata la classifica
     * @throws RemoteException
     */
    public void update(String city) throws RemoteException {
        doCallbacks(city);
    }

    private synchronized void doCallbacks(String city) throws RemoteException {
        // Itero su tutti i client della struttura dati che si sono registrati al servizio di notifica
        for (CallbackHOTELIERClient client: clients.keySet()) {
            // Per ogni client, controllo se hanno registrato il proprio interesse per la città indicata
            // In caso positivo, lo notifico, passando come parametro la città e la classifica degli hotel aggiornata
            if (clients.get(client).contains(city))
                client.notifyEvent(city, localRanking.get(city).stream().map(Hotel::getName).toList());
        }
    }


    // Metodi dichiarati nell'interfaccia HOTELIERServerInterface
    public boolean login(String username, String password)
        throws UserNotRegisteredException, UserAlreadyLoggedInException, InvalidPasswordException, WrongPasswordException {
        if (password == null) throw new InvalidPasswordException();
        if (!users.containsKey(username)) throw new UserNotRegisteredException();
        if (!users.get(username).verifyPassword(password)) throw new WrongPasswordException();
        synchronized (onlineUsers) {
            if (onlineUsers.contains(username)) throw new UserAlreadyLoggedInException();
            return onlineUsers.add(username);
        }
    }

    public boolean logout(String username) throws UserNotRegisteredException, UserNotLoggedInException {
        if (!users.containsKey(username)) throw new UserNotRegisteredException();
        synchronized (onlineUsers) {
            if (!onlineUsers.contains(username)) throw new UserNotLoggedInException();
            return onlineUsers.remove(username);
        }
    }

    public String searchHotel(String hotel, String city) throws InvalidCityException {
        return listToString(search(hotel, city));
    }

    public String searchAllHotels(String city) throws InvalidCityException {
        if (!localRanking.containsKey(city)) throw new InvalidCityException();
        return listToString(localRanking.get(city));
    }

    private List<Hotel> search(String hotel, String city) throws InvalidCityException {
        if (!localRanking.containsKey(city)) throw new InvalidCityException();
        return localRanking.get(city).stream()
                .filter(object -> object.getName().toLowerCase().contains(hotel.toLowerCase()))
                .toList();
    }

    public void insertReview(String hotel, String city, double score, double[] scores, String username)
            throws InvalidHotelException, UserNotRegisteredException, UserNotLoggedInException, InvalidCityException,
            InvalidScoreException, ReviewAlreadyPostedException {
        // La variabile indica il tempo da attendere per postare la prossima recensione, riferita all'hotel indicato
        // e con lo stesso autore
        long time = checkReviewValidity(hotel, username, System.currentTimeMillis());
        if (!users.containsKey(username)) throw new UserNotRegisteredException();
        if (!onlineUsers.contains(username)) throw new UserNotLoggedInException();

        // Controllo che esista effettivamente un unico hotel con quel nome e in quella città
        List<Hotel> hotelList = search(hotel, city);
        if (hotelList.isEmpty()) throw new InvalidHotelException("La recensione non fa riferimento ad alcuna struttura!");
        // Nel caso in cui la lista contenga due hotel è perché il secondo è di tipo "superior", ma essendoci stato
        // comunque un riscontro per la ricerca vuol dire che il primo è quello che si vuole recensire
        // Altrimenti, se la lista ha più di due elementi in questo caso si sta facendo effettivamente riferimento a
        // più strutture e non è possibile pubblicare la recensione
        if (hotelList.size() > 2) throw new InvalidHotelException("La recensione fa riferimento a più strutture differenti!");

        // Controllo che i voti siano validi e che corrispondano alle categorie previste
        if (score < 0 || score > 5) throw new InvalidScoreException("Voto non valido!");
        for (int i = 0; i < Ratings.getnCategories(); i++) if (scores[i] < 0 || scores[i] > 5)
            throw new InvalidScoreException("Voto di categoria non valido!");

        // Controllo se il tempo da attendere sia superiore a 0, in questo caso non è possibile postare la recensione
        if (time > 0L) throw new ReviewAlreadyPostedException((int) time/1000);

        // Creo una nuova recensione e la aggiungo alla lista di recensioni effettuate dallo stesso autore
        // L'inserimento avviene in testa, così da mantenere l'ordinamento decrescente con le recensioni più recenti in alto
        Review review = new Review(username, hotel, score, scores);
        users.get(username).addReview(review);
        // Aggiungo la review anche al database delle recensioni e aggiorno i punteggi per questa struttura
        synchronized (reviews) {
            reviews.get(hotel).addFirst(review);
            hotels.get(hotel).updateAllRatings(score, scores, reviews.get(hotel).size());
        }
        System.out.println(printCurrentDate() + "\treview #" + review.getId() + " by user #"
                + users.get(username).getId() + " has been posted");
    }

    /**
     * <p align="justify">
     *     Verifica se per l'hotel indicato esiste un'altra recensione con lo stesso autore.
     *     In caso affermativo, controlla che sia trascorso il tempo stabilito.
     * </p> <p align="justify">
     *     Restituisce il tempo da attendere per pubblicare la prossima recensione, se è scaduto il timeout {@code 0L}
     *     in modo da consentire la pubblicazione.
     * </p>
     * @param hotel l'hotel da recensire
     * @param username l'utente autore della recensione
     * @param curr_time il tempo corrente in millisecondi
     * @return il tempo in millisecondi da attendere per pubblicare la prossima recensione
     * @throws InvalidHotelException se l'hotel indicato non esiste
     */
    private long checkReviewValidity(String hotel, String username, long curr_time) throws InvalidHotelException {
        if (!reviews.containsKey(hotel)) throw new InvalidHotelException();
        synchronized (reviews) {
            for (Review element: reviews.get(hotel)) if (element.getAuthor().equals(username))
                // Per ciascuna review riferita all'hotel, se l'autore di questa è l'utente indicato, controllo se il tempo
                // trascorso dall'ultima recensione è minore del timeout
                if ((curr_time - element.getTime()) < RankingHandler.getTimeout())
                    // In caso positivo, ritorno il tempo ancora da attendere
                    return RankingHandler.getTimeout() - (curr_time - element.getTime());
        }
        // Se:
        // - l'hotel non ha recensioni,
        // - l'utente non ha fatto recensioni per questo hotel,
        // - l'utente ha fatto almeno un'altra recensione ma è trascorso il timeout,
        // allora ritorno 0, nonché il tempo da attendere
        return 0L;
    }


    /**
     * <p align="justify">
     *     Cerca l'hotel indicato nella città indicata.
     *     Se esiste, restituisce la lista di recensioni pubblicate per l'hotel indicato.
     * </p>
     * @param hotel l'hotel di cui mostrare le recensioni
     * @param city la città in cui è situato l'hotel
     * @return una stringa che rappresenta la lista di recensioni pubblicate per l'hotel
     * @throws InvalidCityException se la città non è una di quelle selezionabili previste
     * @throws InvalidHotelException se l'hotel indicato non esiste
     */
    public String showAllReviews(String hotel, String city) throws InvalidCityException, InvalidHotelException {
        if (!localRanking.containsKey(city)) throw new InvalidCityException();
        if (!localRanking.get(city).contains(hotels.get(hotel))) throw new InvalidHotelException();
        List<Hotel> hotelList = search(hotel, city);
        // Caso analogo all'inserimento di una recensione
        if (hotelList.isEmpty()) throw new InvalidHotelException();
        if (hotelList.size() > 2) throw new InvalidHotelException("Il nome della struttura non è corretto.");
        return listToString(reviews.get(hotel));
    }

    /**
     * Vota la recensione indicata.
     * @param reviewId id della recensione da votare
     * @param session la sessione attiva corrispondente all'utente o al guest che vota la recensione
     * @return {@code true} se l'utente o guest non ha ancora votato la recensione, {@code false} altrimenti
     * @throws InvalidVoteException se il voto non è valido, per cui è l'autore a votare la recensione
     * @throws InvalidReviewException se la recensione non esiste
     */
    public boolean upvote(String reviewId, String session) throws InvalidVoteException, InvalidReviewException {
        int id = Integer.parseInt(reviewId);
        synchronized (reviews) {
            for (List<Review> reviews: reviews.values()) for (Review review: reviews)
                if (review.getId() == id) {
                    if (review.getAuthor().equals(session)) throw new InvalidVoteException();
                    else return review.addUpvote(session);
                }
        }
        throw new InvalidReviewException();
    }

    /**
     * <p align="justify">
     *     Restituisce la lista di recensioni pubblicate dall'utente individuato mediante il nome utente indicato.
     * </p>
     * @param username il nome utente
     * @return una stringa che rappresenta la lista di recensioni pubblicate dall'utente
     * @throws UserNotRegisteredException se l'utente non è registrato
     * @throws UserNotLoggedInException se l'utente non ha effettuato l'accesso
     */
    public String showMyReviews(String username) throws UserNotRegisteredException, UserNotLoggedInException {
        if (!users.containsKey(username)) throw new UserNotRegisteredException();
        if (!onlineUsers.contains(username)) throw new UserNotLoggedInException();
        return listToString(users.get(username).getReviews());
    }

    public String showMyBadges(String username) throws UserNotRegisteredException, UserNotLoggedInException {
        if (!users.containsKey(username)) throw new UserNotRegisteredException();
        if (!onlineUsers.contains(username)) throw new UserNotLoggedInException();
        return users.get(username).getBadge();
    }


    // Metodi per il calcolo e l'aggiornamento dei local ranking
    /**
     * <p align="justify">
     *     Calcola e aggiorna i punteggi degli hotel per stilare la classifica locale (local ranking) per ogni città.
     *     Restituisce una {@code HashMap} che per ogni città ha definita la classifica degli hotel aggiornata.
     *     </p> <p align="justify">
     *     Ordina gli hotel della stessa città in ordine decrescente in base al punteggio ottenuto.
     * </p>
     * @return la classifica aggiornata degli hotel per ogni città
     */
    public HashMap<String, String[]> updateRankings() throws IOException {
        long time = System.currentTimeMillis(); // tempo corrente da usare per le chiamate di funzioni successive

        // Sincronizzo sulla struttura così da non consentire eventuali letture
        synchronized (localRanking) {
            // Per ciascun hotel, calcolo la funzione per aggiornare il ranking di quell'hotel
            localRanking.values().forEach(hotels -> hotels.forEach(hotel -> rankingAlgorithm(hotel, time)));

            HashMap<String, String[]> hotels = new HashMap<>();
            // Per ciascun hotel presente nella lista degli hotel di una città
            for (List<Hotel> hotelList: localRanking.values()) {
                // Ordino la lista confrontando i rankings degli hotel: la lista è ordinata in ordine non crescente
                hotelList.sort(Comparator.comparingInt(Hotel::getRanking).reversed());
                // Assegno a ciascun hotel così ordinato la posizione corrispondente nella classifica
                hotelList.forEach(element -> element.setRank(hotelList.indexOf(element) + 1));
                hotels.put(hotelList.get(0).getCity(), (String[]) hotelList.stream().map(Hotel::getName).toArray());
            }
            return hotels;
        }
    }

    /**
     * Calcola il punteggio (ranking) di un hotel.
     * <p> Dato un hotel, per ogni recensione <i>i</i> utilizza la funzione: </p>
     * <p align="center"> <i> f<sub>i</sub>(t) = R<sub>i</sub>/(1 + t) + log<sub>2</sub> U<sub>i</sub> </i>, </p>
     * <p align="justify">
     *     in cui la costante <i> R<sub>i</sub> </i> rappresenta il punteggio sintetico della recensione scalato
     *     nell'intervallo tra 0 e 100; <i> U<sub>i</sub> </i> sono i voti utili ricevuti della recensione; <i> t </i>
     *     è l'intervallo di tempo che intercorre tra il tempo di pubblicazione della recensione e {@code time}.
     * </p> <p align="justify">
     *     Successivamente per ogni hotel, si ha
     *     <i> f(t) = n + ∑<sub>i</sub> f<sub>i</sub>(t) </i>,
     *     dove <i>n</i> è il numero totale di recensioni pubblicate per l'hotel.
     * </p>
     * @param hotel l'hotel di cui viene calcolato il punteggio
     * @param time il tempo corrente in millisecondi
     */
    private void rankingAlgorithm(Hotel hotel, long time) {
        double partial = 0;
        double weight = 0;
        int mod_time; // mod_time è il tempo trascorso dal tempo di pubblicazione della recensione

        synchronized (reviews) {
            // Per ciascuna review nella lista, calcola la funzione per aggiornare il ranking ed eventualmente la classifica
            for (Review review: reviews.get(hotel.getName())) {
                mod_time = (int) (time - review.getTime()) / 1000; // divido per 1000 per ottenere i secondi
                partial += review.getRate() * 20 / (1 + mod_time); // moltiplico il punteggio per 20 così da scalarlo nel range [0, 100]
                weight += (int) (Math.log(review.getnUpvotes()) / Math.log(2)); // calcolo il logaritmo in base 2 dei voti
            }
            // Assegno il nuovo ranking ottenuto all'hotel, sommando i valori ottenuti e arrotondo a intero la somma
            hotel.setRanking((int) Math.round(reviews.size() + partial + weight));
        }
    }


    // Metodi per il backup
    /**
     * <p align="justify">
     *     Effettua il backup degli utenti sul file indicato.
     * </p>
     * @param objectMapper l'object mapper per la serializzazione dei dati
     * @param file il file di destinazione per il backup
     * @throws IOException
     */
    public void usersBackup(ObjectMapper objectMapper, File file) throws IOException {
        synchronized (users) {
            objectMapper.writeValue(file, users.values());
        }
    }

    /**
     * <p align="justify">
     *     Effettua il backup degli hotels sul file indicato.
     * </p>
     * @param objectMapper l'object mapper per la serializzazione dei dati
     * @param file il file di destinazione per il backup
     * @throws IOException
     */
    public void hotelsBackup(ObjectMapper objectMapper, File file) throws IOException {
        synchronized (hotels) {
            objectMapper.writeValue(file, hotels.values());
        }
    }


    // Altri metodi
    /**
     * <p align="justify">
     *     Restituisce una stringa che rappresenta gli elementi della lista indicata sottoforma di sotto-stringhe.
     * </p> <p align="justify">
     *     Per ottenere la rappresentazione degli elementi della lista come stringhe viene utilizzato il metodo
     *     {@link Object#toString()}.
     * </p>
     * @param list una lista di qualsiasi tipo
     * @return una stringa che rappresenta gli elementi della lista
     */
    public String listToString(List<?> list) {
        StringBuilder builder = new StringBuilder();
        for (Object item: list) builder.append(item.toString()).append("\n");
        return builder.toString().trim();
    }

    /**
     * Calcola e formatta l'ora e la data corrente.
     * @return una stringa che rappresenta la data e l'ora corrente nel formato indicato {@code yyyy/MM/dd HH:mm:ss}
     */
    public static String printCurrentDate() {
        return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(System.currentTimeMillis()));
    }
}