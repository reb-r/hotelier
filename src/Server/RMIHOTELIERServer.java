package Server;

import MyExceptions.*;
import Client.CallbackHOTELIERClient;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;


/**
 * <p align="justify">
 *     L'interfaccia {@code RMIHOTELIERServer} estende l'interfaccia {@link Remote} e offre un insieme di servizi remoti.
 * </p>
 */
public interface RMIHOTELIERServer extends Remote {
    /** Nome pubblico del servizio */
    String SERVICE_NAME = "HOTELIER";

    /**
     * <p align="justify">
     *     Registra un nuovo utente al servizio, con nome utente e password indicati.
     * </p>
     * @param username il nome utente
     * @param password la password
     * @return {@code true} se la registrazione è andata a buon fine, {@code false} altrimenti
     * @throws UsernameAlreadyTakenException se l'utente è già registrato
     * @throws InvalidPasswordException se la password è {@code null}, non valida
     */
    boolean register(String username, String password)
            throws RemoteException, UsernameAlreadyTakenException, InvalidPasswordException;

    /**
     * <p>
     *     Registra il client al servizio di notifica tramite callback.
     * </p>
     * @param stub il riferimento all'oggetto remoto del client
     * @param cities la lista di città per cui si richiede la notifica tramite callback
     * @return una lista contenente in ordine di città le liste degli hotel delle stesse città indicate o {@code null}
     * se c'è stato un errore e non è stata specificata alcuna città
     * @throws RemoteException
     */
    List<List<String>> registerForCallback(CallbackHOTELIERClient stub, List<String> cities) throws RemoteException;

    /**
     * <p>
     *     Cancella la registrazione del client dal servizio di notifica tramite callback.
     * </p>
     * @param stub il riferimento all'oggetto remoto del client
     * @throws RemoteException
     */
    void unregisterForCallback(CallbackHOTELIERClient stub) throws RemoteException;
}