package MyExceptions;

public class InvalidHotelException extends Exception {
    public InvalidHotelException() { super("Hotel non valido!"); }
    public InvalidHotelException(String msg) { super("Hotel non valido!\n" + msg); }
}


