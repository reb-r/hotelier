package Server.config;

import Server.Database.Hotel.City;
import Server.Database.Hotel.Hotel;
import Server.Database.User;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import static Client.HOTELIERClient.formatPassword;

public class JsonFile {
    static final String HOTEL_FILENAME = "src/Server/config/hotel_database.json";
    static final String USER_FILENAME = "src/Server/config/user_database.json";

    public static void main(String[] args) {
        LinkedList<Hotel> hotelList = new LinkedList<>();
        for (City city: City.values()) for (Hotel.Type type: Hotel.Type.values()) hotelList.add(new Hotel(city, type));
        try {
            writeFile(hotelList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeFile(LinkedList<Hotel> hotelList) throws IOException {
        File hotel_file = new File(HOTEL_FILENAME);
        if (hotel_file.exists()) hotel_file.delete();
        hotel_file.createNewFile();

        File user_file = new File(USER_FILENAME);
        if (user_file.exists()) user_file.delete();
        user_file.createNewFile();

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter("    ", DefaultIndenter.SYS_LF);
        DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
        prettyPrinter.indentObjectsWith(indenter);
        prettyPrinter.indentArraysWith(indenter);
        objectMapper.setDefaultPrettyPrinter(prettyPrinter);
        try {
            objectMapper.writeValue(hotel_file, hotelList);
            objectMapper.writeValue(user_file, new User[]{new User("admin", formatPassword("admin"))});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
