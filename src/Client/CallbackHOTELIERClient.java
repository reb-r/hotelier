package Client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;


/**
 * <p align="justify">
 *     L'interfaccia {@code CallbackHOTELIERClient} estende l'interfaccia {@link Remote} e consente di utilizzare il
 *     meccanismo delle callback per la ricezione di notifiche relative agli eventi per cui questo client ha registrato
 *     il proprio interesse.
 * </p>
 */
public interface CallbackHOTELIERClient extends Remote {
    /**
     * <p align="justify">
     *     Notifica e stampa sul client la nuova classifica aggiornata per la città indicata.
     * </p>
     * @param city la città per cui si è registrato interesse e la cui classifica è stata aggiornata
     * @param hotels la lista di hotel presenti nella città
     */
    void notifyEvent(String city, List<String> hotels) throws RemoteException;
}