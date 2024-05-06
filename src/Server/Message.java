package Server;

import Client.HOTELIERClient;

import java.util.Arrays;

import static Server.Message.Reply.Session.*;
import static Server.Message.Request.Method.*;


/**
 * <p align="justify">
 *     La classe {@code Message} rappresenta i messaggi che possono essere scambiati tra client e server e il loro formato.
 * </p>
 */
public class Message {
    /**
     * <p align="justify">
     *     La classe {@code Request} rappresenta il messaggio di tipo richiesta inviato da un client al server.
     * </p> <p align="justify">
     *     Specifica il formato che devono avere i messaggi di richiesta.
     * </p>
     */
    public static class Request {
        /**
         * <p align="justify">
         *     Definisce il messaggio di richiesta da inviare al server in base al comando e ai parametri indicati.
         * </p>
         * @param command il comando che rappresenta il servizio richiesto
         * @param args un insieme di parametri, che verranno utilizzati per elaborare il messaggio di richiesta
         * @return il messaggio di richiesta
         */
        public static String getMessage(HOTELIERClient.Command command, Object... args) {
            return switch (command) {
                // message = LOGIN username password
                case LOGIN -> (args.length == LOGIN.parameters)?
                        LOGIN.method + " " + args[0] + " " + args[1] : "";
                // message = LOGOUT user:username
                case LOGOUT -> (args.length == LOGOUT.parameters)?
                        LOGOUT.method + " " + USER.session + args[0] : "";
                // message = SEARCH hotel city
                case SEARCH -> (args.length == SEARCH.parameters)?
                        SEARCH.method + " \"" + args[0] + "\" \"" + args[1] + "\"" : "";
                // message = SEARCHALL city
                case SEARCHALL -> (args.length == SEARCHALL.parameters)?
                        SEARCHALL.method + " " + args[0]: "";
                // message = INSERTREVIEW user:username hotel city score scores
                case INSERTREVIEW -> (args.length == INSERTREVIEW.parameters)?
                        INSERTREVIEW.method + " " +  USER.session + args[0] + " \"" + args[1] + "\" \"" + args[2] + "\" " + args[3]
                                + " " + Arrays.toString((double[]) args[4]).replaceAll("[,\\[\\]]", "") : "";
                // message = SHOWMYBADGES user:username
                case SHOWMYBADGES -> (args.length == SHOWMYBADGES.parameters)?
                        SHOWMYBADGES.method + " " + USER.session + args[0] : "";
                // message = SHOWREVIEWS hotel city
                case SHOWREVIEWS -> (args.length == SHOWREVIEWS.parameters)?
                        SHOWREVIEWS.method + " \"" + args [0] + "\" " + args[1] : "";
                // message = UPVOTE #review
                case UPVOTE -> (args.length == UPVOTE.parameters)? UPVOTE.method + " " + args[0] : "";
                // message = SHOWMYREVIEWS user:username
                case SHOWMYREVIEWS -> (args.length == SHOWMYREVIEWS.parameters)?
                        SHOWMYREVIEWS.method + " " + USER.session + args[0] : "";
                default -> "";
            };
        }


        /**
         * La classe {@code Method} rappresenta l'insieme di servizi che possono essere richiesti al server.
         * @see #LOGIN
         * @see #LOGOUT
         * @see #SEARCH
         * @see #SEARCHALL
         * @see #INSERTREVIEW
         * @see #SHOWMYBADGES
         * @see #UPVOTE
         * @see #SHOWREVIEWS
         * @see #SHOWMYREVIEWS
         */
        public enum Method {
            /** <p> Metodo per il login </p>
             * Richiede due parametri. */
            LOGIN("LOGIN", 2),
            /** <p> Metodo per il logout </p>
             * Richiede due parametri. */
            LOGOUT("LOGOUT", 1),
            /** <p> Metodo per la ricerca di un hotel </p>
             * Richiede due parametri. */
            SEARCH("SEARCH", 2),
            /** <p> Metodo per la ricerca degli hotel per città </p>
             * Richiede un parametro. */
            SEARCHALL("SEARCHALL", 1),
            /** <p> Metodo per la pubblicazione di una recensione </p>
             * Richiede cinque parametri. */
            INSERTREVIEW("INSERTREVIEW", 5),
            /** <p> Metodo per la visualizzazione dei badges ottenuti </p>
             * Richiede un parametro. */
            SHOWMYBADGES("SHOWMYBADGES", 1),
            /** <p> Metodo per la visualizzazione delle recensioni di un hotel </p>
             * Richiede due parametri. */
            SHOWREVIEWS("SHOWREVIEWS", 2),
            /** <p> Metodo per la votazione di una recensione </p>
             * Richiede un parametro. */
            UPVOTE("UPVOTE", 1),
            /** <p> Metodo per la visualizzazione delle recensioni pubblicate </p>
             * Richiede un parametro. */
            SHOWMYREVIEWS("SHOWMYREVIEWS", 1);

            /** Metodo */
            private final String method;
            /** Parametri richiesti dal metodo */
            private final int parameters;

            Method(String method, int parameters) {
                this.method = method;
                this.parameters = parameters;
            }

            public static Method fetchMethod(String constant) {
                for (Method method: Method.values()) if (method.method.equals(constant)) return method;
                return null;
            }
        }
    }


    /**
     * <p align="justify">
     *     La classe {@code Reply} rappresenta il messaggio di tipo risposta inviato dal server a un client.
     * </p> <p align="justify">
     *     Specifica il formato che devono avere i messaggi di risposta.
     * </p>
     */
    public static class Reply {
        /**
         * <p align="justify">
         *     La classe {@code Status} rappresenta i due possibili stati del servizio richiesto dopo l'elaborazione.
         * </p>
         * @see #SUCCESS
         * @see #ERROR
         */
        public enum Status {
            SUCCESS,
            ERROR;

            public String toString() { return this.name() + " "; }
        }


        /**
         * <p align="justify">
         *     La classe {@code Info} è l'insieme delle descrizioni di {@link Status#SUCCESS}.
         * </p>
         * @see #OK
         * @see #DONE
         * @see #FAILURE
         * @see #FOUND
         * @see #MORE
         * @see #NOTFOUND
         */
        public enum Info {
            OK("OK"),
            DONE("Done"),
            FAILURE("Failure"),
            FOUND("Found"),
            MORE("More"),
            NOTFOUND("NotFound");

            public final String info;

            Info(String info) { this.info = info; }
        }


        /**
         * <p align="justify">
         *     La classe {@code Error} rappresenta i possibili errori che si possono verificare durante l'elaborazione
         *     del messaggio di risposta ed è parte dell'insieme delle descrizioni di {@link Status#ERROR}.
         * </p>
         * @see #LOGINERROR
         * @see #LOGOUTERROR
         * @see #SEARCHERROR
         * @see #SEARCHALLERROR
         * @see #INSERTREVIEWERROR
         * @see #SHOWMYBADGESERROR
         * @see #SHOWREVIEWSERROR
         * @see #SHOWMYREVIEWSERROR
         * @see #BADREQUESTERROR
         * @see #SESSIONERROR
         */
        public enum Error {
            LOGINERROR("LoginError", "Errore durante il login!"),
            LOGOUTERROR("LogoutError", "Errore durante il logout!"),
            SEARCHERROR("SearchError", "Errore durante la ricerca!"),
            SEARCHALLERROR("SearchAllError", "Errore durante la ricerca totale!"),
            INSERTREVIEWERROR("InsertReviewError", "Errore durante la pubblicazione della recensione!"),
            SHOWMYBADGESERROR("ShowMyBadgesError", "Errore durante la visualizzazione del distintivo!"),
            SHOWREVIEWSERROR("ShowReviewsError", "Errore durante la visualizzazione delle recensioni dell'hotel!"),
            SHOWMYREVIEWSERROR("ShowMyReviewSError", "Errore durante la visualizzazione delle recensioni!"),
            BADREQUESTERROR("BadRequestError", "Errore durante l'elaborazione della richiesta!"),
            SESSIONERROR("SessionError", "Errore di sessione!");

            /** Tipo di errore */
            public final String err;
            /** Messaggio da visualizzare per l'errore corrispondente */
            public final String msg;

            Error(String error, String message) {
                this.err = error;
                this.msg = message;
            }

            @Override
            public String toString() { return err + "\n" + msg; }
        }


        /**
         * <p align="justify">
         *     La classe {@code Session} è l'insieme degli elementi che forniscono informazioni relative al tipo di
         *     sessione in uso e vanno sempre seguiti da un identificatore: nome utente o indirizzo IP per un guest.
         * </p>
         * @see #SESSION
         * @see #GUEST
         * @see #USER
         */
        public enum Session {
            SESSION("session:"),
            GUEST("guest:"),
            USER("user:");

            public final String session;

            Session(String session) { this.session = session; }

            @Override
            public String toString() { return session; }
        }
    }
}