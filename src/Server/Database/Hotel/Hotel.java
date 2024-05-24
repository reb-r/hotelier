package Server.Database.Hotel;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;


/**
 * La classe {@code Hotel} rappresenta gli hotel su cui si basa il servizio.
 */
public class Hotel implements Serializable {
    @Serial
    private static final long serialVersionUID = 2882170010415255273L;
    private static int N_HOTELS; // record di tipo Hotel

    /** Campi significativi della classe */
    private static final int N_FIELDS = 10;

    /** Identificatore univoco dell'hotel */
    private int id;
    /** Nome dell'hotel */
    private String name;
    /** Indirizzo */
    private String address;
    /** Città */
    private String city;
    /** Tipo/classe (numero di stelle) */
    private String type;
    /** Numero di telefono */
    private String phone;
    /** Descrizione testuale */
    private String description;
    /** Servizi offerti */
    private List<String> features;
    /** Punteggio sintetico (rate) */
    private double rate;
    /** Punteggi per categoria (ratings) */
    private Ratings ratings;
    /** Posizione nella classifica locale */
    private transient int rank = 0;
    /** Punteggio totale */
    private transient int ranking = 0;


    public Hotel(City city, Type type) {
        Random generator = new Random();

        // Assegno i parametri
        this.city = city.name;
        this.type = type.toString();

        // Assegno l'id generato automaticamente al campo corrispondente
        setId(city);

        // Assegno il nome dell'hotel come concatenazione di "Hotel", città e tipo
        this.name = "Hotel " + this.city + " " + type.type;

        // Assegno l'indirizzo in modo casuale, concatenando la stringa relativa alla via con un numero (civico)
        Address[] addresses = Address.values();
        this.address = addresses[generator.nextInt(Address.N_ADDRESSES)].toString() + " " + generator.nextInt(999);

        // Assegno il numero di telefono
        setPhone(city);

        // Assegno la descrizione in modo casuale
        Description[] descriptions = Description.values();
        this.description = descriptions[generator.nextInt(Description.N_DESCRIPTIONS)].toString().concat(this.city).concat(".");

        // Inizializzo la lista di servizi dell'hotel
        this.features = new LinkedList<>();
        setFeatures();

        // Inizializzo il rate e i ratings
        this.rate = 0;
        this.ratings = new Ratings();
    }

    public Hotel() { }


    // Metodi set (per l'inizializzazione)
    /**
     * <p align="justify">
     *     Assegna l'id a questo hotel.
     * </p>
     * @param city la città in cui è situato questo hotel
     */
    private void setId(City city) {
        // La prima cifra dell'id rappresenta la città in cui è situato l'hotel, le ultime la quantità di hotel presenti
        // (alla sua creazione)
        String id = String.valueOf(city.ordinal() + 1);
        if (id.length() == 1) id = id.concat(String.format("%04d", ++N_HOTELS));
        else id = id.concat(String.format("%03d", ++N_HOTELS));
        this.id = Integer.parseInt(id);
    }

    /**
     * <p align="justify">
     *     Assegna il numero di telefono a questo hotel.
     * </p>
     * @param city la città in cui è situato questo hotel
     */
    private void setPhone(City city) {
        // Concatena il prefisso della città a un numero casuale, in modo da avere sempre un numero di 10 cifre
        Random generator = new Random();
        phone = city.prefix + switch (city.prefix.length()) {
            case 3 -> String.format("%08d", generator.nextInt(99999999));
            case 4 -> String.format("%07d", generator.nextInt(9999999));
            case 5 -> String.format("%06d", generator.nextInt(999999));
            default -> "";
        };
    }

    /**
     * <p align="justify">
     *     Assegna in modo casuale un insieme di servizi offerti possibili a questo hotel.
     * </p>
     */
    private void setFeatures() {
        Random generator = new Random();
        Feature[] features = Feature.values();
        int size = generator.nextInt(Feature.N_FEATURES); // scelgo in modo casuale un numero di feature da assegnare
        if (size == 0) if (generator.nextDouble() >= 0.5) size++; // se la quantità è 0, con probabilità 0.5 viene incrementata a 1
        while (size > 0) {
            String feature = features[generator.nextInt(Feature.N_FEATURES)].toString(); // scelgo in modo casuale la feature
            if (!this.features.contains(feature)) { // se non è già presente tra quelle assegnate in precedenza
                this.features.add(feature);  // aggiungo la feature alla lista
                size--;
            }
        }
    }


    // Metodi get e set
    public static int getnFields() { return N_FIELDS; }

    public String getName() { return name; }

    public String getCity() { return city; }

    public String getType() { return type; }

    public int getRank() { return rank; }

    public int getRanking() { return ranking; }

    public void setRank(int rank) { this.rank = rank; }

    public void setRanking(int ranking) { this.ranking = ranking; }


    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Hotel hotel)) return false;
        return (id == hotel.id);
    }

    @Override
    public String toString() {
        return "{" + name + "; " + address + "; " + city + "; " + type + "; " + phone + "; " + description + "; "
                + features.toString() + "; " + rate + "; " + ratings.toString() + "; " + rank + "}";
    }


    // Altri metodi
    /**
     * <p align="justify">
     *     Aggiorna il rate con il voto indicato e aggiorna i ratings per categoria con i voti indicati.
     * </p>
     * @param score il voto con cui aggiornare il rate
     * @param scores i voti con cui aggiornare i ratings
     * @param size il numero totale di recensioni pubblicate per questo hotel
     */
    public synchronized void updateAllRatings(double score, double[] scores, int size) {
        double average = (((size - 1) * rate) + score)/size; // calcolo la media con l'aggiunta del nuovo rate
        rate = (double) Math.round((average * 10))/10.0; // arrotondamento della media a una cifra decimale
        ratings.updateRatings(scores, size);
    }


    /**
     * <p align="justify">
     *     La classe {@code Type} rappresenta il tipo (classe) dell'hotel, corrispondente al numero di stelle.
     * </p>
     * @see #STARS_3
     * @see #STARS_4
     * @see #STARS_4S
     * @see #STARS_5
     * @see #STARS_5S
     */
    public enum Type {
        /** Tre stelle */
        STARS_3("3", 3),
        /** Quattro stelle */
        STARS_4("4", 4),
        /** Quattro stelle superior */
        STARS_4S("4S", 4),
        /** Cinque stelle */
        STARS_5("5", 5),
        /** Cinque stelle superior */
        STARS_5S("5S", 5);

        public static final int N_TYPES = Type.values().length;
        /** Tipo (classe) corrispondente al numero di stelle dell'hotel */
        final String type;
        /** Stelle per tipo */
        final int stars;

        Type(String type, int stars) {
            this.type = type;
            this.stars = stars;
        }

        public int getStars() { return stars; }

        @Override
        public String toString() { return stars + " stelle" + (type.contains("S")? " superior" : ""); }

        /**
         * <p align="justify">
         *     Effettua il parsing della stringa indicata, restituendo il tipo corrispondente.
         * </p>
         * @param constant la stringa di cui effettuare il parsing
         * @return il tipo corrispondente
         */
        public static Type parseType(String constant) {
            for (Type type: Type.values()) if (type.toString().equals(constant)) return type;
            return null;
        }
    }

    public enum Address {
        ADDRESS_1("Vicolo Corto"),
        ADDRESS_2("Vicolo Stretto"),
        ADDRESS_3("Bastioni Gran Sasso"),
        ADDRESS_4("Viale Monterosa"),
        ADDRESS_5("Viale Vesuvio"),
        ADDRESS_6("Via Accademia"),
        ADDRESS_7("Corso Ateneo"),
        ADDRESS_8("Piazza Università"),
        ADDRESS_9("Via Verdi"),
        ADDRESS_10("Corso Raffaello"),
        ADDRESS_11("Piazza Dante"),
        ADDRESS_12("Via Marco Polo"),
        ADDRESS_13("Corso Magellano"),
        ADDRESS_14("Largo Colombo"),
        ADDRESS_15("Viale Costantino"),
        ADDRESS_16("Viale Traiano"),
        ADDRESS_17("Piazza Giulio Cesare"),
        ADDRESS_18("Via Roma"),
        ADDRESS_19("Corso Impero"),
        ADDRESS_20("Largo Augusto"),
        ADDRESS_21("Viale dei Giardini"),
        ADDRESS_22("Parco della Vittoria");

        public static final int N_ADDRESSES = Address.values().length;
        final String address;

        Address(String address) { this.address = address; }

        @Override
        public String toString() { return address; }
    }

    public enum Description {
        DESCRIPTION_1("Un hotel moderno con vista mozzafiato a "),
        DESCRIPTION_2("Un hotel ristrutturato a due passi dal centro di "),
        DESCRIPTION_3("Un hotel storico situato nel centro di "),
        DESCRIPTION_4("Un hotel vicino alle principali attrazioni turistiche di "),
        DESCRIPTION_5("Un hotel elegante nei pressi di ");

        public static final int N_DESCRIPTIONS = Description.values().length;
        final String description;

        Description(String description) { this.description = description; }

        @Override
        public String toString() { return description; }
    }

    public enum Feature {
        ARIA_CONDIZIONATA("Aria condizionata"),
        CANCELLAZIONE_GRATUITA("Cancellazione gratuita"),
        CENTRO_BENESSERE("Centro benessere"),
        COLAZIONE_INCLUSA("Colazione inclusa"),
        FRIGO_IN_CAMERA("Frigo in camera"),
        PAGAMENTO_IN_STRUTTURA("Pagamento in struttura"),
        PARCHEGGIO_GRATUITO("Parcheggio gratuito"),
        PALESTRA("Palestra"),
        PISCINA_AL_CHIUSO("Piscina al chiuso"),
        RISTORANTE("Ristorante"),
        SAUNA("Sauna"),
        TV_IN_CAMERA("TV in camera"),
        WI_FI("Wi-Fi");

        public static final int N_FEATURES = Feature.values().length;
        final String feature;

        Feature(String feature) {
            this.feature = feature;
        }

        @Override
        public String toString() {
            return feature;
        }
    }
}