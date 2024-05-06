import Client.GUIHOTELIERCustomerClient;
import Client.HOTELIERCustomerClient;

import org.apache.commons.cli.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.NotBoundException;
import java.util.Properties;


public class HOTELIERClientMain {
    static final String CONFIG_FILE = "src/Client/config/client.properties";
    static final String CLI_OPTION = "cli";
    static final String GUI_OPTION = "gui";
    static String server_address;
    static int connection_port;
    static int registry_port;
    static String multicast_address;
    static int multicast_port;


    public static void main(String[] args) {
        // Leggo il file di configurazione
        try {
            readConfig();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("HOTELIERClientMain.main(String[] args): an error occurred while reading configuration file");
        }

        // Definisco le opzioni che possono essere usate da linea di comando
        Options options = new Options();
        Option opt_cli = new Option(CLI_OPTION, false, "starts client with a command line interface");
        opt_cli.setRequired(false);
        options.addOption(opt_cli);
        Option opt_gui = new Option(GUI_OPTION, false, "starts client with a dedicated graphical user interface");
        opt_gui.setRequired(false);
        options.addOption(opt_gui);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        try {
            // Parso gli argomenti
            CommandLine cmd = parser.parse(options, args);
            Thread client;
            // Controllo se è stata specificata l'opzione per l'uso della CLI, altrimenti avvio la versione con GUI
            if (cmd.hasOption(CLI_OPTION))
                client = new Thread(new HOTELIERCustomerClient(server_address, connection_port, registry_port, multicast_address, multicast_port));
            else
                client = new Thread(new GUIHOTELIERCustomerClient(server_address, connection_port, registry_port, multicast_address, multicast_port));
            client.start();
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("javac HOTELIERClientMain", options);
            System.exit(1);
        } catch (NotBoundException | IOException e) {
            System.out.println("Impossibile contattare il server. Si prega di riprovare.");
        }
    }

    /**
     * Legge il file di configurazione.
     * @throws IOException
     */
    public static void readConfig() throws IOException {
        InputStream input = new FileInputStream(CONFIG_FILE);
        Properties properties = new Properties();
        properties.load(input);

        server_address = properties.getProperty("server_address");
        connection_port = Integer.parseInt(properties.getProperty("connection_port"));
        registry_port = Integer.parseInt(properties.getProperty("registry_port"));
        multicast_address = properties.getProperty("multicast_address");
        multicast_port = Integer.parseInt(properties.getProperty("multicast_port"));

        input.close();
    }
}