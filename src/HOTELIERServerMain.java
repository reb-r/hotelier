import Server.HOTELIERServer;

import java.io.*;
import java.rmi.AlreadyBoundException;
import java.util.Properties;


public class HOTELIERServerMain {
    static final String CONFIG_FILE = "src/Server/config/server.properties";
    static int listening_port;
    static int registry_port;
    static String multicast_address;
    static int multicast_port;
    static String hotel_database;
    static String user_database;
    static String file_format;
    static String config_filepath;
    static String backup_filepath;
    static long ranking_timeout;
    static long backup_timeout;

    public static void main(String[] args) {
        // Leggo il file di configurazione
        try {
            readConfig();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        // Avvio il server
        try {
            new HOTELIERServer(listening_port, registry_port, hotel_database, user_database, file_format,
                    config_filepath, backup_filepath, ranking_timeout, backup_timeout, multicast_address, multicast_port);
        } catch (AlreadyBoundException | IOException e) {
            System.out.println(e.getMessage());
            System.exit(1);
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

        listening_port = Integer.parseInt(properties.getProperty("listening_port"));
        registry_port = Integer.parseInt(properties.getProperty("registry_port"));
        multicast_address = properties.getProperty("multicast_address");
        multicast_port = Integer.parseInt(properties.getProperty("multicast_port"));
        hotel_database = properties.getProperty("hotel_filename");
        user_database = properties.getProperty("user_filename");
        config_filepath = properties.getProperty("config_filepath");
        backup_filepath = properties.getProperty("backup_filepath");
        file_format = properties.getProperty("file_format");
        ranking_timeout = Long.parseLong(properties.getProperty("ranking_timeout"));
        backup_timeout = Long.parseLong(properties.getProperty("backup_timeout"));

        input.close();
    }
}