package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.Collections;

/**
 * This is the class used for defining the order in which the deliveries should be made.
 */
public class DeliveryOrder {


    /**
     * This method sorts a list of orders in a specific way. It sorts them by "value per cost" first.
     * Then, it rearranges them so those which are closer together on the map are closer in the list.
     * @param dailyOrders the list of orders to be sprted
     * @param words the Object connecting to the web server. It's needed for translating the what3word addresses
     *              to  longitude-latitude pairs
     * @return a list of orders containing the same orders as dailyOrders, but sorted differently.
     */
    public static ArrayList<Order> sortDeliveryOrder(ArrayList<Order> dailyOrders, HttpWords words) {
        dailyOrders.sort(new Order.SortByValuePerMove());
        LongLat location0;
        ArrayList<LongLat> destinations= new ArrayList<>();
        ArrayList<Double> distances = new ArrayList<Double>();

        int closest = 0;
        for (int i = 0; i < dailyOrders.size() - 4; i++) {
            location0 = words.w3wToLongLat(dailyOrders.get(i).deliverTo);
            destinations.add( words.w3wToLongLat(dailyOrders.get(i + 1).restaurantPlace.get(0)) ) ;
            destinations.add ( words.w3wToLongLat(dailyOrders.get(i + 2).restaurantPlace.get(0)) );
            destinations.add( words.w3wToLongLat(dailyOrders.get(i + 3).restaurantPlace.get(0)));
            distances.add( location0.distanceTo(destinations.get(0)));
            distances.add( location0.distanceTo(destinations.get(1)));
            distances.add( location0.distanceTo(destinations.get(2)));
            if (distances.get(0) > distances.get(1)) {
                closest = 1;
            }
            if (distances.get(closest) > distances.get(2)) {
                closest = 2;
            }
            if (! (closest == 0)) {
                Collections.swap(dailyOrders, i+1, i+1+closest);
            }
        }
        return dailyOrders;
    }
}
