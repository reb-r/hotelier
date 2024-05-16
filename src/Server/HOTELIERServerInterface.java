package Server;

import MyExceptions.*;


/**
 *  L'interfaccia {@code HOTELIERServerInterface} definisce l'insieme di servizi offerti dal server.
 */
public interface HOTELIERServerInterface {
    /**
     * Effettua il login al profilo utente.
     * <p align="justify">
     *     L'accesso va a buon fine se e soltanto se entrambi nome utente e password indicati corrispondono a quelli
     *     memorizzati per l'utente individuato da {@code username}.
     * </p>
     * @param username il nome utente
     * @param password la password
     * @return {@code true} se l'accesso è andato a buon fine, {@code false} altrimenti
     * @throws UserNotRegisteredException se l'utente non è registrato
     * @throws UserAlreadyLoggedInException se l'utente ha già effettuato l'accesso
     * @throws InvalidPasswordException se la password è {@code null}, non valida
     * @throws WrongPasswordException se la password è errata, non corrisponde a quella dell'utente
     */
    boolean login(String username, String password)
            throws UserNotRegisteredException, UserAlreadyLoggedInException, InvalidPasswordException, WrongPasswordException;

    /**
     * Effettua il logout dal profilo utente individuato da {@code username}.
     * @param username il nome utente
     * @return {@code true} se il logout è andato a buon fine, {@code false} altrimenti
     * @throws UserNotRegisteredException se l'utente non è registrato
     * @throws UserNotLoggedInException se l'utente non ha effettuato l'accesso in precedenza
     */
    boolean logout(String username) throws UserNotRegisteredException, UserNotLoggedInException;

    /**
     * <p align="justify">
     *     Cerca tra gli hotel di una città quelli che nel nome contengono la stringa {@code hotel} e restituisce la
     *     lista cone le informazioni relative agli hotel che soddisfano i criteri della ricerca.
     * </p> <p align="justify">
     *     La ricerca prevede un match parziale, non case-sensitive e in cui è sufficiente che la stringa {@code hotel}
     *     sia contenuta all'interno del nome degli hotel presenti nella città indicata in cui viene effettuata la ricerca.
     * </p>
     *
     * @param hotel l'hotel da cercare
     * @param city  la città in cui cercare l'hotel
     * @return una stringa che rappresenta la lista degli hotel che soddisfano i criteri della ricerca
     * @throws InvalidCityException se la città non è una di quelle selezionabili previste
     */
    String searchHotel(String hotel, String city) throws InvalidCityException;

    /**
     * <p align="justify">
     *     Trova nella città tutti gli hotel situati in questa e restituisce la lista con le informazioni relative agli
     *     hotel presenti nella città.
     * </p>
     *
     * @param city la città in cui ricercare gli hotel
     * @return una stringa che rappresenta la lista degli hotel che soddisfano i criteri della ricerca
     * @throws InvalidCityException se la città non è una di quelle selezionabili previste
     */
    String searchAllHotels(String city) throws InvalidCityException;

    /**
     * <p align="justify">
     *     Pubblica una nuova recensione per l'hotel della città.
     * </p> <p align="justify">
     *     La recensione da pubblicare deve contenere i voti da assegnare all'hotel,
     *     rispettivamente il punteggio sintetico e i punteggi per categoria.
     * </p>
     * @param hotel l'hotel da recensire
     * @param city la città in cui è situato l'hotel
     * @param score il punteggio sintetico
     * @param scores i punteggi per categoria
     * @param username l'utente autore della recensione
     * @throws InvalidHotelException se l'hotel da recensire non esiste, se non corrisponde ad alcuna struttura o
     * fa riferimento a più strutture
     * @throws UserNotRegisteredException se l'utente non è registrato
     * @throws UserNotLoggedInException se l'utente non ha effettuato l'accesso
     * @throws InvalidCityException se la città non è una di quelle selezionabili previste
     * @throws InvalidScoreException se i punteggi non sono conformi, cioè non sono compresi tra 0 e 5
     * @throws ReviewAlreadyPostedException se l'utente ha già postato di recente una recensione per la stessa struttura
     */
    void insertReview(String hotel, String city, double score, double[] scores, String username)
            throws InvalidHotelException, UserNotRegisteredException, UserNotLoggedInException, InvalidCityException,
            InvalidScoreException, ReviewAlreadyPostedException;

    /**
     * <p align="justify">
     *     Restituisce il distintivo dell'utente individuato dal nome utente indicato, corrispondente al massimo livello
     *     di esperienza raggiunto.
     * </p>
     * @param username l'utente di cui si richiede il badge
     * @return una stringa che rappresenta il distintivo dell'utente
     * @throws UserNotRegisteredException se l'utente non è registrato
     * @throws UserNotLoggedInException se l'utente non ha effettuato l'accesso
     */
    String showMyBadges(String username) throws UserNotRegisteredException, UserNotLoggedInException;
}