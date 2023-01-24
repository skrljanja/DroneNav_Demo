package uk.ac.ed.inf;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

/**
 * This is the class used for connecting to the words part of the web server. It is used for translating what3words
 * addresses of places to LongLat objects.
 */
public class HttpWords {
    String machine;
    String port;

    public HttpWords(String machineName, String portName) {
        machine = machineName;
        port = portName;
    }

    /**
     * The fields of these 2 internal classes correspond exactly to the fields a w3w entry in the JSON file has. It allows us
     * to easily obtain the values from the web servers. (some fields here are marked as unused, but they are necessary!)
     */
    public static class What3Words {
        String country;
        Coordinates square;
        String nearestPlace;
        Coordinates coordinates;
        String words;
        String language;
        String map;
    }
    public static class Coordinates {
        String lng;
        String lat;
    }

    /**
     * This method gets a what3words location as an argument. it finds it in the words table on the website
     * data, where it retrieves the longitude and latitude of its' centre. An object of LongLat with the central
     * longitude and latitude is returned.
     * @param w3w some location in what3words format (word.word.word)
     * @return a LongLat object that corresponds to the w3w location
     */
    public LongLat w3wToLongLat(String w3w) {
        String[] w3wList  = w3w.split("\\.");
        String urlString = "http://" + this.machine + ":" + this.port + "/words/"
                + w3wList[0] + "/" + w3wList[1] + "/" + w3wList[2] + "/" + "details.json";
        What3Words dst = null;
        HttpResponse<String> response = null;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .build();
        try {
            response = HttpClientAll.client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 404) {
                System.err.println("Error 404:" + urlString + " is not found. Check for typos.");
                throw new FileNotFoundException("Error 404:" + urlString + " is not found. Check for typos.");
            }
        } catch (Exception e)  {
            System.err.println("This menu database cannot be accessed. Check that the machine and port fields are correct.");
        }
        assert response != null;
        if (response.statusCode() == 200) { // the response.body() is only used if the response.statusCode tells us the request was valid (200)
            Type W3WType = new TypeToken<What3Words>() {}.getType();
            dst = new Gson().fromJson(response.body(), W3WType);
        }
        LongLat result = ( new LongLat(Double.parseDouble(Objects.requireNonNull(dst).coordinates.lng), Double.parseDouble(Objects.requireNonNull(dst).coordinates.lat)));
        return result;
    }
}
