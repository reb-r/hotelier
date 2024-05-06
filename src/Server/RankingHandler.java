package Server;

import java.io.IOException;
import java.net.*;
import java.rmi.RemoteException;
import java.util.*;


/**
 * <p align="justify">
 *     La classe {@code RankingHandler} estende la classe {@link Thread} e si occupa della gestione del calcolo dei
 *     ranking degli hotel e dell'aggiornamento periodico della classifica di questi ultimi.
 * </p>
 */
public class RankingHandler extends Thread {
    /** Intervallo di tempo per l'aggiornamento della classifica */
    private static long timeout;

    /** Istanza del server */
    private final HOTELIERServer server;
    /** Struttura dati per le classifiche locali */
    private final HashMap<String, String[]> localRanking;
    // lista di coppie <nome città, lista di hotel in ordine di ranking> aggiornata periodicamente

    /** Datagram socket */
    private final DatagramSocket socket;
    /** Indirizzo di multicast */
    private final InetAddress multicast_address;
    /** Porta di multicast */
    private final int multicast_port;
    /** Buffer per l'invio dei pacchetti */
    private byte[] buffer;

    public RankingHandler(long timeout, HOTELIERServer server, String multicast_address, int multicast_port)
            throws UnknownHostException, SocketException {
        // Assegno i parametri alle variabili
        RankingHandler.timeout = timeout;
        this.server = server;
        this.multicast_address = InetAddress.getByName(multicast_address);
        this.multicast_port = multicast_port;
        this.localRanking = new HashMap<>();

        // Costruisco la datagram socket per l'invio dei messaggi di notifica per l'aggiornamento delle classifiche
        socket = new DatagramSocket(65535);
    }

    @Override
    public void run() {
        while (Thread.currentThread().isAlive()) {
            try {
                Thread.sleep(timeout);
                System.out.println(HOTELIERServer.printCurrentDate() + "\tupdating rankings...");
                // Aggiorno i ranking
                HashMap<String, String[]> hotels = server.updateRankings();
                // Dopo l'aggiornamento, per ogni città controllo se la classifica è rimasta invariata
                for (String city: localRanking.keySet()) if (!Arrays.equals(hotels.get(city), localRanking.get(city))) {
                    // Notifico i client che hanno registrato interesse per quella città (perché si è aggiornata la classifica)
                    server.update(city);
                    // Controllo se è cambiato il primo classificato per inviare un messaggio sul gruppo di multicast
                    if (!hotels.get(city)[0].equals(localRanking.get(city)[0]))
                        sendMessage(city, localRanking.get(city)[0], hotels.get(city)[0]);
                }
                // Aggiorno la struttura dati e mi metto in attesa per il tempo stabilito
                localRanking.putAll(hotels);
            } catch (InterruptedException | RemoteException e) {
                System.out.println(HOTELIERServer.printCurrentDate()
                        + "\tHOTELIERServer.RankingHandler.run(): an error occurred while updating rankings");
            } catch (IOException e) {
                System.out.println(HOTELIERServer.printCurrentDate()
                        + "\tHOTELIERServer.RankingHandler.run(): an error occurred while sending multicast datagrams");
            }
        }
    }

    /**
     * <p align="justify">
     *     Invia un messaggio sul gruppo di multicast, notificando l'aggiornamento del primo classificato nella città indicata.
     * </p>
     * @param city la città in cui sono situati gli hotel
     * @param oldHotel il vecchio hotel primo in classifica
     * @param newHotel l'attuale hotel primo in classifica
     * @throws IOException
     */
    private void sendMessage(String city, String oldHotel, String newHotel) throws IOException {
        buffer = ("Notifica evento: aggiornamento prima posizione a " + city + "\n" + oldHotel + " -> " + newHotel
                + " (NEW)").getBytes();
        DatagramPacket datagram = new DatagramPacket(buffer, buffer.length, multicast_address, multicast_port);
        socket.send(datagram);
    }


    // Metodo get
    public static long getTimeout() { return timeout; }
}
