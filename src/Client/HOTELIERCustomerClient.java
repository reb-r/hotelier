package Client;

import MyExceptions.*;
import Server.Database.*;
import Server.Database.Hotel.*;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.*;

import static Client.HOTELIERClient.Command.*;
import static Server.Database.Hotel.Hotel.Type.*;
import static Server.Message.Request;
import static Server.Message.Reply.Status.*;
import static Server.Message.Reply.Info.*;


/**
 * <p align="justify">
 *     La classe {@code HOTELIERCustomerClient} estende la classe astratta {@link HOTELIERClient} e gestisce
 *     l'interazione con l'utente tramite CLI.
 * </p>
 */
public class HOTELIERCustomerClient extends HOTELIERClient {
    /** Scanner per lo stream di input */
    private final Scanner stream = new Scanner(System.in);
    /** Input */
    private String input; // ultimo dato immesso

    public HOTELIERCustomerClient(String server_address, int connection_port, int registry_port,
                                  String multicast_address, int multicast_port) throws IOException, NotBoundException {
        super(server_address, connection_port, registry_port);
        multicast = new MulticastClient(multicast_address, multicast_port);
    }

    @Override
    public void run() {
        welcome(); // stampo il messaggio di benvenuto
        Command command;
        boolean check = true; // variabile di controllo per la stampa di inizio riga

        while (channel.isConnected()) { // finché il canale è aperto e connesso
            if (check) System.out.print("> ");
            else check = true;

            // Determino il comando inserito dall'utente
            if ((command = Command.parseCommand(input = stream.nextLine())) == null)
                System.out.println("Attenzione: il comando inserito non è valido! Se hai bisogno di aiuto digita 'help' e riprova.");
            else switch (command) {
                case REGISTER:
                    // Controllo se non ha effettuato l'accesso, altrimenti non può registrarsi
                    if (!isLoggedIn()) register();
                    else System.out.println("Errore: per poterti registrare con un altro nome utente devi prima disconnetterti.");
                    break;
                case LOGIN:
                    // Controllo se non ha già effettuato l'accesso, altrimenti non può fare la login
                    if (!isLoggedIn()) tryLogin();
                    else System.out.println("Errore: hai già effettuato l'accesso!");
                    break;
                case LOGOUT:
                    // Controllo se non ha effettuato l'accesso perché in questo caso non può disconnettersi
                    if (!isLoggedIn()) System.out.println("Errore: per eseguire questo comando devi prima effettuare l'accesso!");
                    else if (logout()) menu();
                    break;
                case SEARCH: searchHotel();
                    break;
                case SEARCHALL: searchAllHotels();
                    break;
                case INSERTREVIEW:
                    // Controllo se ha effettuato l'accesso, altrimenti non può pubblicare una recensione
                    if (isLoggedIn()) insertReview();
                    else System.out.println("Errore: per eseguire questo comando devi prima effettuare l'accesso!");
                    break;
                case SHOWMYBADGES:
                    // Controllo se ha effettuato l'accesso, altrimenti non può visualizzare il badge
                    if (isLoggedIn()) showMyBadges();
                    else System.out.println("Errore: per eseguire questo comando devi prima effettuare l'accesso!");
                    break;
                case SHOWREVIEWS:
                    showAllReviews();
                    System.out.print("> ");
                    // Il comando UPVOTE è utilizzabile soltanto dopo aver fatto richiesta delle recensioni di un hotel
                    while (stream.hasNext(UPVOTE.command)) {
                        upvote(input = stream.nextLine().split(" ", 2)[1]);
                        System.out.print("> ");
                    }
                    check = false;
                    break;
                case SHOWMYREVIEWS:
                    // Controllo se ha effettuato l'accesso, altrimenti non può visualizzare le proprie recensioni
                    if (isLoggedIn()) showMyReviews();
                    else System.out.println("Errore: per eseguire questo comando devi prima effettuare l'accesso!");
                    break;
                case HELP: help();
                    break;
                case CANCEL: break;
                case QUIT:
                    // Controllo se ha effettuato l'accesso e se il logout è andato a buon fine per uscire dall'applicazione
                    if (isLoggedIn()) if (!logout()) break;
                    quit();
            }
        }
        System.out.println("La connessione con il server è stata interrotta, l'applicazione verrà chiusa.");
        quit();
    }


    public synchronized void notifyEvent(String city, List<String> hotels) throws RemoteException {
        System.out.println("Notifica evento: aggiornamento classifica di " + city);
        List<String> ranking = getRanking(city); // classifica non aggiornata
        // Aggiorno la classifica
        setRanking(city, hotels);
        // Stampo il messaggio di notifica su schermo
        for (int i = 0; i < Hotel.Type.N_TYPES; i++) {
            if (ranking.get(i).equals(hotels.get(i))) System.out.printf("%d. %s -> %s\n", i + 1, ranking.get(i), hotels.get(i));
            else System.out.printf("%d. %s -> %s (NEW)\n", i + 1, ranking.get(i), hotels.get(i));
        }
    }

    /**
     * <p align="justify">
     *     Registra un nuovo utente con nome utente e password richiesti. Attende finché non viene inserito l'input.
     * </p> <p align="justify">
     *     Dopo aver effettuato la registrazione, invia automaticamente al server una richiesta per effettuare
     *     l'accesso con il nome utente e la password usati per la registrazione.
     * </p>
     */
    private void register() {
        System.out.println("Inserisci nome utente e password che vuoi usare per il prossimo accesso.");
        // Richiedo il nome utente per la registrazione
        System.out.print("Username: ");
        while (stream.hasNext()) { // attendo l'inserimento
            if (isCancel(input = stream.nextLine())) { // se l'input è cancel, esco
                System.out.println("Attenzione: non è possibile utilizzare questo nome utente! Riprova.");
                return;
            } else if (input.matches("[ \"]")) { // se contiene caratteri non ammessi, chiedo un nuovo input
                System.out.println("Attenzione: sono presenti caratteri non ammessi!");
                System.out.print("Username: ");
            } else break; // altrimenti proseguo
        }
        String username = input; // assegno l'input
        // Richiedo la password per la registrazione
        System.out.print("Password: ");
        while (stream.hasNext()) {
            if ((input = stream.nextLine()).matches("[ \"]")) {
                System.out.println("Attenzione: la password non può contenere spazi!");
                System.out.print("Password: ");
            } else if (input.length() < 5) {
                System.out.println("Attenzione: la password deve essere lunga almeno 5 caratteri.");
                System.out.print("Password: ");
            } else break;
        }
        String password = input;
        // Richiedo la conferma della password per la registrazione
        System.out.print("Conferma password: ");
        while (!stream.hasNext(password)) {
            stream.nextLine();
            System.out.print("La password non corrisponde. Riprova.\nConferma password: ");
        }
        // Codifico la password, prima di inviare la richiesta al server
        if ((password = formatPassword(input = stream.nextLine())) == null) password = input;
        try {
            // Se la registrazione va a buon fine, effettuo l'accesso
            if (server.register(username, password)) {
                System.out.println("Registrazione completata con successo!");
                login(username, password);
            } else System.out.println("Si è verificato un errore durante la registrazione. Si prega di riprovare.");
        } catch (InvalidPasswordException | UsernameAlreadyTakenException e) {
            System.out.println(e.getMessage());
        } catch (RemoteException e) {
            exit("Si è verificato un errore.");
        }
    }

    /**
     * <p align="justify">
     *     Richiede username e password con cui si intende effettuare l'accesso. Attende finché non viene inserito l'input.
     * </p> <p align="justify">
     *     Successivamente effettua l'accesso con i dati inseriti.
     * </p>
     */
    private void tryLogin() {
        System.out.println("Inserisci nome utente e password.");
        // Richiedo il nome utente per il login
        System.out.print("Username: ");
        if (isCancel(input = stream.nextLine())) return;
        String username = input;
        // Richiedo la password per il login
        System.out.print("Password: ");
        String password;
        if ((password = formatPassword(input = stream.nextLine())) == null) password = input;
        login(username, password);
    }

    /**
     * <p align="justify">
     *     Invia una richiesta al server da parte del client per effettuare l'accesso con nome utente e password.
     * </p> <p align="justify">
     *     Una volta effettuato l'accesso, rende disponibile il client alla ricezione di messaggi multicast e lo
     *     registra per la ricezione delle notifiche di aggiornamento dei rankings locali.
     * </p>
     *
     * @param username il nome utente con cui si intende effettuare l'accesso
     * @param password la password corrispondente al profilo dell'utente individuato da {@code username}
     */
    private void login(String username, String password) {
        System.out.println("Accesso in corso...");
        try {
            // Invio il messaggio di richiesta al server e gestisco la risposta
            String[] reply = sendRequest(Request.getMessage(LOGIN, username, password));
            if (reply == null) return;
            if (reply.length > 0) {
                // Effettuo il parsing della risposta secondo lo schema [STATUS Info Body] dove
                // STATUS rappresenta il risultato della richiesta [SUCCESS|ERROR]
                // Info è la descrizione del risultato:
                // - SUCCESS -> [OK]
                // - ERROR -> [Error|Exception]
                // Body contiene ulteriori informazioni a seconda del risultato
                // - SUCCESS -> user:username
                // - ERROR -> error message
                if (reply[0].startsWith(SUCCESS.toString())) { // se il login è andato a buon fine
                    System.out.println("Accesso effettuato correttamente!");
                    // Imposto la sessione per l'utente loggato
                    setSession(reply[1].split(":")[1]);
                    // Rendo disponibile questo client alla ricezione di messaggi multicast e di notifiche di callback
                    // Avvio il multicast client se non è gia stato avviato
                    if (!multicast.isAlive()) multicast.start();
                    multicast.subscribe();
                    registerForCallback();
                } else System.out.println(reply[1]); // altrimenti stampo il messaggio di errore
            } else System.out.println("Si è verificato un errore durante l'accesso. Si prega di riprovare.");
        } catch (IOException e) {
            exit("Si è verificato un errore durante l'accesso.");
        }
    }


    /**
     * <p align="justify">
     *     Registra il client alla ricezione di notifiche per l'aggiornamento dei rankings locali.
     * </p> <p align="justify">
     *     Richiede le città di interesse per cui intende essere notificato. Attende finché non viene inserito l'input.
     * </p> <p align="justify">
     *     Questo passaggio è comunque opzionale.
     * </p>
     * @throws RemoteException
     */
    private void registerForCallback() throws RemoteException {
        List<String> values = new ArrayList<>(Arrays.stream(City.values()).map(City::getName).toList());
        System.out.println("Inserisci le città di interesse per cui intendi ricevere notifiche sull'aggiornamento dei relativi ranking locali.");
        System.out.println("Premi INVIO dopo aver inserito il nome di una città e digita 'confirm' quando hai concluso la selezione.");
        System.out.println("Ecco l'elenco delle città disponibili:");
        for (String city: values) System.out.printf("\t%s %s\n", ">", city); // stampo l'elenco delle città disponibili
        System.out.println("Se desideri saltare questo passaggio digita 'skip' in qualsiasi momento.");

        String city;
        List<String> cities = new LinkedList<>();
        while (true) {
            System.out.print("> ");
            if ((input = stream.nextLine()).isBlank()) continue; // se l'input è vuoto, continuo
            if (isCancel(input)) break; // se è cancel, esco
            else if (input.equals("skip")) { // se è skip, mostro il menu ed esco
                menu();
                break;
            } else if (input.equals("confirm")) { // se è confirm
                // Controllo che l'insieme delle città selezionate non sia vuoto, altrimenti lo comunico
                if (!cities.isEmpty()) {
                    // Imposto l'insieme delle città di interesse per questo utente e lo registro per la callback
                    setLocalRanking(cities);
                    List<List<String>> hotels = server.registerForCallback(stub, cities);
                    // Controllo che la registrazione alla callback sia andata a buon fine
                    if (hotels != null) {
                        // Stampo su schermo l'insieme delle città di interesse per cui l'utente si è registrato
                        for (int i = 0; i < cities.size(); i++) setRanking(cities.get(i), hotels.get(i));
                        System.out.println("Città per cui desideri ricevere aggiornamenti in questa sessione: "
                                + cities.toString().replaceAll("[\\[|\\]]", ""));
                    } else System.out.println("Si è verificato un errore durante l'impostazione delle notifiche.");
                } else {
                    System.out.println("Non hai inserito alcuna città di interesse. "
                            + "Se desideri modificare quest'impostazione dovrai effettuare il logout e accedere nuovamente.");
                    menu();
                }
                break;
            } else { // se è stato inserito altro
                // Controllo che l'ìnput corrisponda a una delle città disponibili, altrimenti comunico l'errore
                city = (input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase().trim());
                if (values.contains(city)) cities.add(city);
                else System.out.println("Attenzione: devi scegliere tra una delle città a disposizione!");
            }
        }
    }

    /**
     * <p align="justify">
     *     Invia una richiesta al server per effettuare il logout dell'utente loggato.
     *     Resta in attesa della conferma da parte dell'utente.
     * </p> <p align="justify">
     *     Questo metodo è accessibile solamente nel caso in cui l'utente abbia effettuato l'accesso su questo client.
     * </p>
     * @return {@code true} se l'utente è stato disconnesso con successo, {@code false} altrimenti o se l'utente ha
     * annullato l'operazione
     */
    private boolean logout() {
        System.out.print("Sei sicuro di voler uscire? [yes\\no]\n> ");
        // Chiedo conferma all'utente per effettuare il logout e controllo l'input
        if ((input = stream.nextLine()).equalsIgnoreCase("yes") || input.equalsIgnoreCase("y")) {
            System.out.println("Disconnessione in corso...");
            try {
                // Invio il messaggio di richiesta al server e gestisco la risposta
                String[] reply = sendRequest(Request.getMessage(LOGOUT, getSession()));
                if (reply == null) return false;
                if (reply.length > 0) {
                    // Effetto il parsing della risposta secondo lo schema [STATUS Info Body] dove
                    // STATUS rappresenta il risultato della richiesta [SUCCESS|ERROR]
                    // Info è la descrizione del risultato:
                    // - SUCCESS -> [Done]
                    // - ERROR -> [Error|Exception]
                    // Body contiene ulteriori informazioni solamente nel caso seguente
                    // - SUCCESS -> (empty)
                    // - ERROR -> error message
                    if (reply[0].startsWith(SUCCESS.toString())) { // se il logout è andato a buon fine
                        // Resetto le variabili relative alla sessione e alle città di interesse
                        setSession(null);
                        setLocalRanking(null);
                        // Annullo l'iscrizione alla ricezione di messaggi multicast
                        multicast.unsubscribe();
                        // Annullo la registrazione per le notifiche di callback
                        server.unregisterForCallback(stub);
                        System.out.println("Sei stato disconnesso.");
                        return true;
                    } else System.out.println(reply[1]);
                } else System.out.println("Si è verificato un errore durante il logout. Si prega di riprovare.");
            } catch (IOException e) {
                exit("Si è verificato un errore durante il logout.");
            }
        } else System.out.println("Operazione annullata. Se hai bisogno di aiuto digita 'help'.");
        return false;
    }

    /**
     * <p align="justify">
     *     Richiede il nome dell'hotel e la città in cui effettuare la ricerca. Attende finché non viene inserito l'input.
     * </p> <p align="justify">
     *     Successivamente invia una richiesta al server da parte del client per ottenere le informazioni relative agli
     *     hotel che soddisfano i criteri della ricerca.
     * </p>
     */
    private void searchHotel() {
        System.out.println("Inserisci il nome dell'hotel e la città in cui vuoi effettuare la ricerca.");
        // Richiede il nome dell'hotel da cercare
        System.out.print("Hotel: ");
        if (isCancel(input = stream.nextLine())) return;
        String hotel = input;
        // Richiede la città in cui cercare l'hotel indicato
        System.out.print("Città: ");
        if (isCancel(input = stream.nextLine())) return;
        String city = input;
        try {
            // Invio il messaggio di richiesta al server
            searchParsing(sendRequest(Request.getMessage(SEARCH, hotel, city)));
        } catch (IOException e) {
            exit("Si è verificato un errore durante la ricerca.");
        }
    }

    /**
     * <p align="justify">
     *     Richiede la città in cui effettuare la ricerca. Attende finché non viene inserito l'input.
     * </p> <p align="justify">
     *     Successivamente invia una richiesta al server da parte del client per ottenere le informazioni relative a
     *     tutti gli hotel situati nella città.
     * </p>
     */
    private void searchAllHotels() {
        System.out.println("Inserisci la città in cui vuoi effettuare la ricerca.");
        // Richiede la città in cui effettuare la ricerca
        System.out.print("Città: ");
        if (isCancel(input = stream.nextLine())) return;
        String city = input;
        try {
            // Invio il messaggio di richiesta al server
            searchParsing(sendRequest(Request.getMessage(SEARCHALL, city)));
        } catch (IOException e) {
            exit("Si è verificato un errore durante la ricerca per città.");
        }
    }

    /**
     * <p align="justify">
     *     Effettua il parsing della risposta da parte del server in seguito alla richiesta di ricerca inviata in
     *     precedenza e la stampa su schermo.
     * </p>
     * @param reply il messaggio di risposta ricevuto dal server
     */
    private void searchParsing(String[] reply) {
        if (reply == null) return;
        if (reply.length > 0) {
            // Effettuo il parsing della risposta secondo lo schema [STATUS Info Body] dove
            // STATUS rappresenta il risultato della richiesta [SUCCESS|ERROR]
            // Info è la descrizione del risultato:
            // - SUCCESS -> [Found|NotFound]
            // - ERROR -> [Error|Exception]
            // Body contiene ulteriori informazioni a seconda del risultato
            // - SUCCESS -> lista di stringhe contenenti le informazioni degli hotel
            // - ERROR -> error message
            if (reply[0].startsWith(SUCCESS.toString())) { // se la ricerca ha avuto successo
                // Controllo che ci sia stato un riscontro e stampo i risultati, altrimenti comunico l'esito
                if (!reply[0].split(" ", 2)[1].equals(NOTFOUND.info)) {
                    System.out.println("La ricerca ha prodotto i seguenti risultati:");
                    printResults(reply[1].split("\n"));
                } else System.out.println("La tua ricerca non ha prodotto alcun risultato.");
            } else System.out.println(reply[1]);
        } else System.out.println("Si è verificato un errore durante la ricerca. Si prega di riprovare.");
    }

    /**
     * <p align="justify">
     *     Stampa su schermo i risultati della ricerca, secondo una formattazione predefinita per hotel.
     * </p>
     * @param results gli elementi da stampare
     */
    private void printResults(String[] results) {
        String[] element;
        // Per ciascun risultato, stampo le informazioni dell'hotel
        for (String result: results) {
            // Identifico i singoli campi suddividendo la stringa
            element = result.replaceAll("[{|}]", "").split(";");
            for (int field = 0; field < Hotel.getnFields(); field++) {
                switch (field) {
                    // NOME
                    case 0: System.out.println(element[field].trim());
                        break;
                    // INDIRIZZO
                    case 1: System.out.printf("\t- %s: %s\n", "Indirizzo", element[field].trim());
                        break;
                    // CITTÀ
                    case 2: System.out.printf("\t- %s: %s\n", "Città", element[field].trim());
                        break;
                    // TIPO
                    case 3: System.out.printf("\t- %s: %s\n", "Tipo", getPrintStars(element[field].trim()));
                        break;
                    // TELEFONO
                    case 4: System.out.printf("\t- %s: %s\n", "Telefono", element[field].trim());
                        break;
                    // DESCRIZIONE
                    case 5: System.out.printf("\t- %s: %s\n", "Descrizione", element[field].trim());
                        break;
                    // SERVIZI
                    case 6:
                        System.out.printf("\t- %s:\n", "Servizi offerti dalla struttura");
                        if (element[field].replaceAll("[\\[\\]]", "").isBlank()) System.out.println("\t\t> N/A");
                        else Arrays.stream(element[field].replaceAll("[\\[\\]]", "").split(","))
                                .forEach(feature -> System.out.printf("\t\t> %s\n", feature.trim()));
                        break;
                    // RATE
                    case 7: System.out.printf("\t- %s: %s\n", "Punteggio complessivo", element[field].trim());
                        break;
                    // RATINGS
                    case 8:
                        System.out.printf("\t- %s\n", "Punteggi per categoria");
                        printRatings(element[field].replaceAll("[\\[|\\]]", "").split(","));
                        break;
                    // RANK
                    case 9: System.out.printf("\t- %s: %s%s %s %d %s %s\n", "Posizionamento", "n.",
                            element[field].trim(), "su", Hotel.Type.N_TYPES, "hotel a", element[2].trim());
                }
            }
        }
    }

    /**
     * <p align="justify">
     *     Effettua il parsing del tipo dell'hotel indicato e restituisce la stringa corrispondente da stampare.
     * </p>
     * @param type il tipo dell'hotel di cui effettuare il parsing
     * @return la stringa che rappresenta il tipo dell'hotel
     */
    private String getPrintStars(String type) {
        return switch (parseType(type)) {
            case STARS_3 -> "***";
            case STARS_4 -> "****";
            case STARS_4S -> "****S";
            case STARS_5 -> "*****";
            case STARS_5S -> "*****S";
        } + " (" + type + ")";
    }

    /**
     * Stampa su schermo i ratings indicati.
     * @param ratings la lista di ratings da stampare
     */
    private void printRatings(String[] ratings) {
        for (int j = 0; j < Ratings.getnCategories(); j++) {
            switch (j) {
                case 0:
                    System.out.printf("\t\t> %2s: %s\n", "Posizione", ratings[j].trim());
                    break;
                case 1:
                    System.out.printf("\t\t> %2s: %s\n", "Pulizia", ratings[j].trim());
                    break;
                case 2:
                    System.out.printf("\t\t> %2s: %s\n", "Servizio", ratings[j].trim());
                    break;
                case 3:
                    System.out.printf("\t\t> %2s: %s\n", "Qualità-prezzo", ratings[j].trim());
                    break;
            }
        }
    }

    /**
     * <p align="justify">
     *     Richiede il nome dell'hotel e la città in cui è situato per postare una recensione. Richiede il punteggio
     *     sintetico e i punteggi per categoria da attribuire all'hotel scelto. Attende finché non viene inserito l'input.
     * </p> <p align="justify">
     *     Successivamente invia una richiesta al server da parte del client e stampa l'esito della stessa.
     * </p> <p align="justify">
     *     Questo metodo è accessibile solamente nel caso in cui l'utente abbia effettuato l'accesso su questo client.
     * </p>
     */
    private void insertReview() {
        System.out.println("Inserisci il nome dell'hotel e la città.");
        // Richiedo il nome dell'hotel da recensire
        System.out.print("Hotel: ");
        if (isCancel(input = stream.nextLine())) return;
        String hotel = input;
        // Richiedo il nome della città in cui è situato l'hotel
        System.out.print("Città: ");
        if (isCancel(input = stream.nextLine())) return;
        String city = input;

        // Definisco le variabili corrispondenti ai punteggi attribuiti dall'utente
        double score;
        double[] scores = new double[4];

        // Richiedo il punteggio complessivo per la struttura
        System.out.println("Inserisci il punteggio complessivo per questa struttura.");
         // Attendo per il prossimo input valido (o cancel)
        if (!hasNextValidInput("Punteggio complessivo")) { // se l'input non è un double allora è cancel
            System.out.println(stream.hasNextDouble());
            stream.nextLine(); // consumo la linea ed esco
            return;
        } else score = stream.nextDouble(); // se l'input è un double, lo assegno alla variabile
        stream.nextLine(); // consumo la linea

        // Il comportamento per la richiesta dei punteggi per categoria è analogo a quello appena mostrato
        System.out.println("Inserisci i singoli punteggi per ciascuna categoria.");
        if (!hasNextValidInput("Posizione")) {
            stream.nextLine();
            return;
        } else scores[0] = stream.nextDouble();
        stream.nextLine();

        if (!hasNextValidInput("Pulizia")) {
            stream.nextLine();
            return;
        } else scores[1] = stream.nextDouble();
        stream.nextLine();

        if (!hasNextValidInput("Servizio")) {
            stream.nextLine();
            return;
        } else scores[2] = stream.nextDouble();
        stream.nextLine();

        if (!hasNextValidInput("Qualità-prezzo")) {
            stream.nextLine();
            return;
        } else scores[3] = stream.nextDouble();
        stream.nextLine();

        try {
            // Invio il messaggio di richiesta al server e gestisco la risposta
            String[] reply = sendRequest(Request.getMessage(INSERTREVIEW, getSession(), hotel, city, score, scores));
            if (reply == null) return;
            if (reply.length > 0) {
                // Effettuo il parsing della risposta secondo lo schema [STATUS Info Body] dove
                // STATUS rappresenta il risultato della richiesta [SUCCESS|ERROR]
                // Info è la descrizione del risultato:
                // - SUCCESS -> [Posted]
                // - ERROR -> [Error|Exception]
                // Body contiene ulteriori informazioni a seconda del risultato
                // - SUCCESS -> informative message
                // - ERROR -> error message
                if (reply[0].startsWith(SUCCESS.toString())) System.out.println("Recensione postata!");
                else System.out.println(reply[1]);
            } else System.out.println("Si è verificato un errore durante la pubblicazione della recensione. Si prega di riprovare");
        } catch (IOException e) {
            exit("Si è verificato un errore durante la pubblicazione della recensione.");
        }
    }

    /**
     * <p align="justify">
     *     Attende che l'input sia un valore di tipo {@code double} o il comando {@link Command#CANCEL}.
     * </p>
     * @param type il formato di input richiesto
     * @return {@code true} se il prossimo token è un tipo {@code double}, {@code false} altrimenti (ovvero se è
     * il comando {@code CANCEL})
     */
    private boolean hasNextValidInput(String type) {
        System.out.printf("%s: ", type);
        while (!(stream.hasNextDouble() || stream.hasNext(CANCEL.command))) {
            stream.nextLine();
            System.out.printf("Attenzione: formato non valido. Riprova.\n%s: ", type);
        }
        return stream.hasNextDouble();
    }


    /**
     * <p align="justify">
     *     Richiede il nome dell'hotel e la città in cui è situato per visualizzare le relative recensioni postate.
     *     Attende finché non viene inserito l'input.
     * </p> <p align="justify">
     *     Successivamente invia una richiesta al server da parte del client e stampa l'esito della stessa.
     * </p>
     */
    private void showAllReviews() {
        System.out.println("Inserisci il nome dell'hotel e la città in cui si trova.");
        // Richiedo il nome dell'hotel di cui visualizzare le recensioni
        System.out.print("Hotel: ");
        if (isCancel(input = stream.nextLine())) return;
        String hotel = input;
        // Richiedo il nome della città in cui è situato l'hotel
        System.out.print("Città: ");
        if (isCancel(input = stream.nextLine())) return;
        String city = input;

        try {
            // Invio il messaggio di richiesta al server e gestisco la risposta
            String[] reply = sendRequest(Request.getMessage(SHOWREVIEWS, hotel, city));
            if (reply == null) return;
            if (reply.length > 0) {
                // Effetto il parsing della risposta secondo lo schema [STATUS Info Body] dove
                // STATUS rappresenta il risultato della richiesta [SUCCESS|ERROR]
                // Info è la descrizione del risultato:
                // - SUCCESS -> [Found|NotFound]
                // - ERROR -> [Error|Exception]
                // Body contiene ulteriori informazioni a seconda del risultato
                // - SUCCESS -> lista di stringhe relative alle recensioni relative all'hotel
                // - ERROR -> error message
                if (reply[0].startsWith(SUCCESS.toString())) { // se ha avuto successo
                    // Controllo che ci sia stato un riscontro e stampo i risultati, altrimenti comunico l'esito
                    if (!reply[0].split(" ", 2)[1].equals(NOTFOUND.info)) {
                        System.out.printf("Ecco le recensioni relative alla struttura %s:\n", hotel);
                        printReviews(reply[1].split("\n"), 2);
                        System.out.println("Per consigliare una recensione puoi digitare 'upvote #' seguito dal numero della recensione.");
                    } else System.out.println("Non ci sono ancora recensioni per questa struttura.");
                } else System.out.println(reply[1]);
            } else System.out.println("Si è verificato un errore durante la visualizzazione delle recensioni. Si prega di riprovare.");
        } catch (IOException e) {
            exit("Si è verificato un errore durante la visualizzazione delle recensioni.");
        }
    }

    /**
     * <p align="justify">
     *     In seguito all'immissione del comando {@link Command#UPVOTE}, controlla che la stringa indicata sia nel
     *     formato corretto.
     * </p> <p align="justify">
     *     Successivamente invia una richiesta al server da parte del client e stampa l'esito.
     * </p>
     * @param id la stringa che rappresenta l'id della recensione da votare, preceduta da '#'
     */
    private void upvote(String id) {
        // Controllo che l'id sia nel formato specificato
        if (!id.matches("#[0-9]+"))
            System.out.println("Attenzione: '#' deve essere seguito da un numero intero che fa riferimento alla recensione dell'hotel!");
        else try {
            // Invio il messaggio di richiesta al server e gestisco la risposta
            String[] reply = sendRequest(Request.getMessage(UPVOTE, input = stream.nextLine().trim()));
            if (reply == null) return;
            if (reply.length > 0) {
                // Effettuo il parsing della risposta secondo lo schema [STATUS Info Body] dove
                // STATUS rappresenta il risultato della richiesta [SUCCESS|ERROR]
                // Info è la descrizione del risultato:
                // - SUCCESS -> [OK|Failure]
                // - ERROR -> [Error|Exception]
                // Body contiene ulteriori informazioni a seconda del risultato
                // - SUCCESS -> [success message|failure message]
                // - ERROR -> error message
                if (reply[0].startsWith(SUCCESS.toString())) { // se la votazione è andata a buon fine
                    // Controllo che il voto sia stato registrato o no, stampando l'esito
                    if (!reply[0].split(" ")[1].equals(FAILURE.info)) System.out.println("Voto registrato correttamente!");
                    else System.out.println("Hai già registrato il tuo voto per questa recensione!");
                } else System.out.println(reply[1]);
            } else
                System.out.println("Si è verificato un errore durante la registrazione del voto. Si prega di riprovare.");
        } catch (IOException e) {
            exit("Si è verificato un errore durante la registrazione del voto.");
        }
    }


    /**
     * <p align="justify">
     *     Invia una richiesta al server per visualizzare le recensioni pubblicate dall'utente loggato e stampa l'esito.
     * </p> <p align="justify">
     *     Questo metodo è accessibile solamente nel caso in cui l'utente abbia effettuato l'accesso su questo client.
     * </p>
     */
    private void showMyReviews() {
        try {
            // Invio il messaggio di richiesta al server e gestisco la risposta
            String[] reply = sendRequest(Request.getMessage(SHOWMYREVIEWS, getSession()));
            if (reply == null) return;
            if (reply.length > 0) {
                // Effettuo il parsing della risposta secondo lo schema [STATUS Info Body] dove
                // STATUS rappresenta il risultato della richiesta [SUCCESS|ERROR]
                // Info è la descrizione del risultato:
                // - SUCCESS -> [Found|NotFound]
                // - ERROR -> [Error|Exception]
                // Body contiene ulteriori informazioni a seconda del risultato
                // - SUCCESS -> lista di stringhe relative alle recensioni postate dall'utente loggato
                // - ERROR -> error message
                if (reply[0].startsWith(SUCCESS.toString())) { // se ha avuto successo
                    // Controllo che ci sia stato un riscontro e stampo i risultati, altrimenti comunico l'esito
                    if (!reply[0].split(" ", 2)[1].equals(NOTFOUND.info)) {
                        System.out.println("Ecco le recensioni che hai pubblicato al momento.");
                        printReviews(reply[1].split("\n"), 1);
                    } else System.out.println("Non hai pubblicato ancora nessuna recensione.");
                } else System.out.println(reply[1]);
            } else System.out.println("Si è verificato un errore durante la visualizzazione delle tue recensioni. Si prega di riprovare.");
        } catch (IOException e) {
            exit("Si è verificato un errore durante la visualizzazione delle tue recensioni.");
        }
    }

    /**
     * <p align="justify">
     *     Stampa su schermo le recensioni, secondo una formattazione predefinita e omettendo il campo indicato.
     * </p>
     * @param reviews gli elementi da stampare
     * @param omitted il campo da omettere che non si intende stampare
     */
    private void printReviews(String[] reviews, int omitted) {
        String[] element;
        for (String result: reviews) {
            element = result.replaceAll("[{|}]", "").split(";");
            for (int field = 0; field < Review.getnFields(); field++) {
                switch (field) {
                    // ID
                    case 0: System.out.printf("%s #%s\n", "Recensione", element[field].trim());
                        break;
                    // AUTORE
                    case 1:
                        if (omitted != field) System.out.printf("\t- %s: %s\n", "Autore", element[field].trim());
                        break;
                    // HOTEL
                    case 2:
                        if (omitted != field) System.out.printf("\t- %s: %s\n", "Hotel", element[field].trim());
                        break;
                    // RATE
                    case 3: System.out.printf("\t- %s: %s\n", "Punteggio complessivo", element[field].trim());
                        break;
                    // RATINGS
                    case 4:
                        System.out.printf("\t- %s\n", "Punteggi per categoria");
                        printRatings(element[field].replaceAll("[\\[|\\]]", "").split(","));
                        break;
                    // DATA
                    case 5: System.out.printf("\t- %s: %s\n", "Data di pubblicazione", element[field].trim());
                        break;
                    // VOTI
                    case 6:
                        if (element[field].trim().equals("0")) System.out.printf("\t- %s: N/A\n", "Voti");
                        else System.out.printf("\t- %s: +%s\n", "Voti", element[field].trim());
                        break;
                }
            }
        }
    }


    /**
     * <p align="justify">
     *     Invia una richesta al server per poter visualizzare il distintivo dell'utente loggato e stampa l'esito.
     * </p>
     * <p align="justify">
     *     Questo metodo è accessibile solo nel caso in cui l'utente abbia effettuato l'accesso su questo client.
     * </p>
     */
    private void showMyBadges() {
        try {
            // Invio il messaggio di richiesta al server e gestisco la risposta
            String[] reply = sendRequest(Request.getMessage(SHOWMYBADGES, getSession()));
            if (reply == null) return;
            if (reply.length > 0) {
                // Effettuo il parsing della risposta secondo lo schema [STATUS Info Body] dove
                // STATUS rappresenta il risultato della richiesta [SUCCESS|ERROR]
                // Info è la descrizione del risultato:
                // - SUCCESS -> [Show]
                // - ERROR -> [Error|Exception]
                // Body contiene ulteriori informazioni a seconda del risultato
                // - SUCCESS -> user badge
                // - ERROR -> error message
                if (reply[0].startsWith(SUCCESS.toString())) System.out.println("Il tuo livello attuale è: '" + reply[1] +"'");
                else System.out.println(reply[1]);
            } else System.out.println("Si è verificato un errore durante la visualizzazione del distintivo. Si prega di riprovare.");
        } catch (IOException e) {
            exit("Si è verificato un errore durante la visualizzazione del distintivo.");
        }
    }


    // Altri metodi
    /**
     * <p align="justify">
     *     Termina l'esecuzione dell'applicazione.
     * </p>
     */
    private void quit() {
        System.out.println("Grazie per averci scelto. Alla prossima!");
        System.exit(0);
    }


    /**
     * <p align="justify">
     *     Stampa il messaggio di errore e termina l'esecuzione dell'applicazione.
     * </p>
     * @param message il messaggio di errore da stampare
     */
    private void exit(String message) {
        System.out.println(message);
        System.out.println("Impossibile contattare il server. L'applicazione verrà chiusa.");
        System.exit(1);
    }

    /**
     * <p align="justify">
     *     Determina se il comando inserito è {@code cancel}.
     * </p>
     * @param command il comando inserito
     * @return {@code true} se il comando è diverso da {@code cancel}, {@code false} altrimenti
     */
    private boolean isCancel(String command) { return command.equals(CANCEL.command); }

    /**
     * <p align="justify">
     *     Stampa il messaggio di benvenuto e il menu.
     * </p>
     */
    private void welcome() {
        System.out.println("Benvenuto su HOTELIER!");
        menu();
    }

    /**
     * <p align="justify">
     *     Stampa il messaggio di aiuto e il menu.
     * </p>
     */
    private void help() {
        System.out.print("Hai bisogno di aiuto? ");
        menu();
    }

    /**
     * <p align="justify">
     *     Stampa il menu dei comandi che possono essere eseguiti al momento.
     * </p>
     */
    private void menu() {
        System.out.println("Ecco l'elenco dei comandi che puoi eseguire:");
        if (!isLoggedIn()) {
            System.out.printf("\t%-15s %s\n", REGISTER.command, REGISTER.description);
            System.out.printf("\t%-15s > %s\n", "", "richiesti: " + REGISTER.parameters);
            System.out.printf("\t%-15s %s\n", LOGIN.command, LOGIN.description);
            System.out.printf("\t%-15s > %s\n", "", "richiesti: " + LOGIN.parameters);
            System.out.printf("\t%-15s %s\n", SEARCH.command, SEARCH.description);
            System.out.printf("\t%-15s > %s\n", "", "richiesti: " + SEARCH.parameters);
            System.out.printf("\t%-15s %s\n", SEARCHALL.command, SEARCHALL.description);
            System.out.printf("\t%-15s > %s\n", "", "richiesti: " + SEARCHALL.parameters);
            System.out.printf("\t%-15s %s\n", SHOWREVIEWS.command, SHOWREVIEWS.description);
            System.out.printf("\t%-15s > %s\n", "", "richiesti: " + SHOWREVIEWS.parameters);
        } else {
            System.out.printf("\t%-15s %s\n", LOGOUT.command, LOGOUT.description);
            System.out.printf("\t%-15s > %s\n", "", "richiesti: " + LOGOUT.parameters);
            System.out.printf("\t%-15s %s\n", SEARCH.command, SEARCH.description);
            System.out.printf("\t%-15s > %s\n", "", "richiesti: " + SEARCH.parameters);
            System.out.printf("\t%-15s %s\n", SEARCHALL.command, SEARCHALL.description);
            System.out.printf("\t%-15s > %s\n", "", "richiesti: " + SEARCHALL.parameters);
            System.out.printf("\t%-15s %s\n", INSERTREVIEW.command, INSERTREVIEW.description);
            System.out.printf("\t%-15s > %s\n", "", "richiesti: " + INSERTREVIEW.parameters);
            System.out.printf("\t%-15s %s\n", SHOWREVIEWS.command, SHOWREVIEWS.description);
            System.out.printf("\t%-15s > %s\n", "", "richiesti: " + SHOWREVIEWS.parameters);
            System.out.printf("\t%-15s %s\n", SHOWMYREVIEWS.command, SHOWMYREVIEWS.description);
            System.out.printf("\t%-15s > %s\n", "", "richiesti: " + SHOWMYREVIEWS.parameters);
            System.out.printf("\t%-15s %s\n", SHOWMYBADGES.command, SHOWMYBADGES.description);
            System.out.printf("\t%-15s > %s\n", "", "richiesti: " + SHOWMYBADGES.parameters);
        }
        System.out.printf("\t%-15s %s\n", HELP.command, HELP.description);
        System.out.printf("\t%-15s %s\n", CANCEL.command, CANCEL.description);
        System.out.printf("\t%-15s %s\n", QUIT.command, QUIT.description);
    }
}