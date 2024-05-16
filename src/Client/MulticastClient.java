package Client;

import Client.GUI.CustomFrame;

import javax.swing.*;
import java.io.IOException;
import java.net.*;

import static javax.swing.JOptionPane.showMessageDialog;


/**
 * <p align="justify">
 *     La classe {@code MulticastClient} estende la classe {@link Thread} e si occupa della gestione dei messaggi
 *     multicast in ricezione.
 * </p>
 */
public class MulticastClient extends Thread {
    /** Indirizzo di multicast */
    public static String multicast_address;
    /** Porta di multicast */
    public static int multicast_port;

    /** Datagram socket per la ricezione dei messaggi multicast */
    private final MulticastSocket socket;
    /** Gruppo di multicast */
    private final InetSocketAddress group;
    /** Interfaccia di rete */
    private final NetworkInterface net_interface;
    /** Buffer per i messaggi */
    private final byte[] buffer = new byte[256];
    /** Iscrizione al gruppo di multicast */
    private boolean subscribed; // per determinare se l'utente ha effettuato l'accesso e quindi si è unito al gruppo
    /** Modalità di interfaccia attiva */
    private boolean gui_mode; // per determinare se è attiva la GUI
    /** GUI frame */
    private CustomFrame frame;


    public MulticastClient(String multicast_address, int multicast_port) throws IOException {
        // Assegno i parametri alle variabili
        MulticastClient.multicast_address = multicast_address;
        MulticastClient.multicast_port = multicast_port;
        // Costruisco la datagram socket per la ricezione dei messaggi di notifica per l'aggiornamento delle classifiche
        socket = new MulticastSocket(multicast_port);
        socket.setSoTimeout(0);
        // Definisco il gruppo di multicast e l'interfaccia di rete
        group = new InetSocketAddress(InetAddress.getByName(multicast_address), multicast_port);
        net_interface = NetworkInterface.getByInetAddress(InetAddress.getByName(multicast_address));
        this.gui_mode = false;
    }

    public MulticastClient(String multicast_address, int multicast_port, CustomFrame frame) throws IOException {
        this(multicast_address, multicast_port); // chiamo il primo costruttore
        // Inizializzo le altre variabili
        this.gui_mode = true;
        this.frame = frame;
    }


    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            // Controllo se è iscritto al gruppo di multicast
            if (subscribed) try {
                // Costruisco un nuovo pacchetto per la ricezione del messaggio
                DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);
                socket.receive(datagram); // resto in attesa del messaggio da ricevere
                // Determino il messaggio ricevuto, suddividendolo in sottostringhe
                String[] message = new String(datagram.getData(), 0, datagram.getLength()).split("\n", 2);
                // Comunico il contenuto del messaggio a seconda del tipo di interfaccia in uso
                if (!gui_mode) {
                    System.out.println("...");
                    System.out.println(message[0] + "\n" + message[1]);
                    System.out.print("> ");
                } else showMessageDialog(frame, message[0] + "\n" + message[1].split(" -> ", 2)[1],
                        "Event notification", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ignored) { }
        }
    }


    /**
     * Si unisce al gruppo di multicast.
     * @throws IOException
     */
    public void subscribe() throws IOException {
        socket.joinGroup(group, net_interface);
        subscribed = true;
    }

    /**
     * Lascia il gruppo di multicast.
     * @throws IOException
     */
    public void unsubscribe() throws IOException {
        subscribed = false;
        socket.leaveGroup(group, net_interface);
    }
}