package uk.ac.ed.inf;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.*;
import java.util.ArrayList;

/**
 * This is the class used for connecting to the menus part of the web server. This has data on the restaurant and their
 * offers stored. It is used to obtain some data about the orders to be completed.
 */
public class HttpMenus {
    public String machine;
    public String port;

    // definition of useful constants
    public static int DELIVERY_COST = 50;

    public HttpMenus (String machineName, String portName) {
        machine = machineName;
        port = portName;
    }

    /**
     * The fields of these 2 inner classes correspond exactly to the fields a menu entry in the JSON file has. It allows us
     * to easily obtain the values from the web servers. (some fields here are marked as unused, but they are necessary!)
     */
    public class MenuEntry {
        public String name;
        public String location;
        public ArrayList<ItemEntry> menu;
    }
    public static class ItemEntry {
        public String item;
        public int pence;
    }


    /**
     * This method takes a variable of the type Order, which has been obtained from the Orders table from the Derby database.
     * It finds the items from the order in the menus file on the Website, and then updates the Order with information about
     * the place of the restaurant(s) the items are ordered from and the price of the order (with delivery included).
     * @param order of type Order, which should have all fields but deliveryCost and restaurantPlaces already initialised.
     * @return true if the order was updated successfully, false otherwise.
     */
    public boolean updateOrder(Order order) {
        ArrayList<String> args = order.items;
        String urlString = "http://" + this.machine + ":" + this.port + "/menus/menus.json";
        ArrayList<MenuEntry> menus = new ArrayList<>();
        HttpResponse<String> response;
        int cost = DELIVERY_COST;
        ArrayList<String> restaurantPlaces = new ArrayList<>();

        // this part of code handles the Http request and response
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .build();
        try {
            response = HttpClientAll.client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 404) {
                System.err.println("Error 404:" + urlString + " is not found. Check for typos.");
                return false;
            }
        } catch (Exception e)  {
            System.err.println("This menu database cannot be accessed. Check that the machine and port fields are correct.");
            return false;
        }
        assert response != null;
        if (response.statusCode() == 200) { // the response.body() is only used if the response.statusCode tells us the request was valid (200)
            Type listType = new TypeToken<ArrayList<MenuEntry>>() {}.getType();
            menus = new Gson().fromJson(response.body(), listType);
        }

        // This part of code loops through the menus for every element of args
        for (MenuEntry entry : menus) {
            for (ItemEntry item_entry : entry.menu) {
                for (String arg : args) {
                    if (arg.equals(item_entry.item)) {
                        cost = cost + item_entry.pence;
                        // check that it is
                        if (! (restaurantPlaces.contains(entry.location)))  {
                            restaurantPlaces.add(entry.location);
                            order.addRestaurant(entry.location);
                        }
                    }
                    break;
                }
            }
        }
        order.deliveryCost = cost;
        return true;


    }

}
