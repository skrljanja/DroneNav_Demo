package uk.ac.ed.inf;

import java.net.http.HttpClient;

/**
 * This is the class that instantiates all the other Http classes. It ensures the client is not instantiated too many
 * times
 */
public class HttpClientAll {
    public static final HttpClient client = HttpClient.newHttpClient();
    HttpWords words;
    HttpMenus menus;
    HttpBuildings buildings;

    public HttpClientAll(String machine, String port) {
        words = new HttpWords(machine, port);
        menus = new HttpMenus(machine, port);
        buildings = new HttpBuildings(machine, port);
    }

}
