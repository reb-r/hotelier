package Server;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


/**
 * <p align="justify">
 *     La classe {@code DatabaseHandler} estende la classe {@link Thread} e si occupa di eseguire periodicamente i
 *     backup delle strutture dati del server.
 * </p>
 */
public class BackupHandler extends Thread {
    static final String hotel_dir = "hotel/";
    static final String user_dir = "user/";
    /** Intervallo di tempo per il backup */
    private static long timeout;

    /** Istanza del server */
    private final HOTELIERServer server;
    /** File di backup per gli utenti */
    private final File user_database;
    /** File di backup per gli hotels */
    private final File hotel_database;

    public BackupHandler(long timeout, HOTELIERServer server, String user_database, String hotel_database,
                         String file_format, String backup_filepath) {
        // Assegno i parametri alle variabili
        BackupHandler.timeout = timeout;
        this.server = server;

        // Creo i file per i backup, con uno schema per il nome e il path di questi
        this.hotel_database = new File(backup_filepath + hotel_dir + hotel_database
                + new SimpleDateFormat("_yyyy-MM-dd_HH-mm-ss").format(new Date(System.currentTimeMillis())) + file_format);
        this.user_database = new File(backup_filepath + user_dir + user_database
                + new SimpleDateFormat("_yyyy-MM-dd_HH-mm-ss").format(new Date(System.currentTimeMillis())) + file_format);
        try {
            this.user_database.createNewFile();
            this.hotel_database.createNewFile();
        } catch (IOException e) {
            System.out.println(HOTELIERServer.printCurrentDate()
                    + "\tServer.HOTELIERServerDatabase.HOTELIERServerDatabase(...): an error occurred while creating user database file");
        }
    }

    @Override
    public void run() {
        System.out.println(HOTELIERServer.printCurrentDate() + "\tpreparing users data for backup...");
        System.out.println(HOTELIERServer.printCurrentDate() + "\tpreparing hotels data for backup...");

        // Definisco l'object mapper per serializzare gli oggetti
        ObjectMapper objectMapper = new ObjectMapper();
        // Imposto le proprietà dell'object mapper
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter("    ", DefaultIndenter.SYS_LF);
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
        printer.indentObjectsWith(indenter);
        printer.indentArraysWith(indenter);
        objectMapper.setDefaultPrettyPrinter(printer);

        while (Thread.currentThread().isAlive()) {
            try {
                // Mi metto in attesa per il tempo stabilito
                Thread.sleep(timeout);
                // Converto gli oggetti destinati al backup in dati JSON e li salvo sui file indicati
                server.usersBackup(objectMapper, user_database);
                server.hotelsBackup(objectMapper, hotel_database);
                System.out.println(HOTELIERServer.printCurrentDate() + "\tbackup completed");
            } catch (InterruptedException e) {
                System.out.println(HOTELIERServer.printCurrentDate()
                        + "\tServer.BackupHandler.run(): an error occurred while waiting for backup");
            } catch (IOException e) {
                System.out.println(HOTELIERServer.printCurrentDate()
                        + "\tServer.BackupHandler.run(): an error occurred while writing data");
            }
        }
    }
}