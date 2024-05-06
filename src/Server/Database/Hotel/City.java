package Server.Database.Hotel;


/**
 * <p align="justify">
 *     La classe {@code City} rappresenta le città capoluoghi di regione in cui sono situati gli hotel del servizio.
 * </p> <p align="justify">
 *     Ogni città è caratterizzata da un prefisso relativo al numero di telefono.
 * </p>
 */
public enum City {
    ANCONA("Ancona", "071-"),
    AOSTA("Aosta", "0165-"),
    BARI("Bari", "080-"),
    BOLOGNA("Bologna","051-"),
    CAGLIARI("Cagliari", "070-"),
    CAMPOBASSO("Campobasso", "0874-"),
    CATANZARO("Catanzaro", "0961-"),
    FIRENZE("Firenze", "055-"),
    GENOVA("Genova", "010-"),
    AQUILA("L'Aquila", "0862-"),
    MILANO("Milano", "02-"),
    NAPOLI("Napoli", "081-"),
    PALERMO("Palermo", "091-"),
    PERUGIA("Perugia", "075-"),
    POTENZA("Potenza", "0791-"),
    ROMA("Roma", "06-"),
    TORINO("Torino", "011-"),
    TRENTO("Trento", "0461-"),
    TRIESTE("Trieste", "040-"),
    VENEZIA("Venezia","041-");

    public static final int N_CITIES = City.values().length;
    /** Nome della città */
    final String name;
    /** Prefisso della città per il numero di telefono */
    final String prefix;

    City(String name, String prefix) {
        this.name = name;
        this.prefix = prefix;
    }

    public String getName() { return name; }

    public String getPrefix() {
        return prefix;
    }
}