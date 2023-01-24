package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Order is the class representing an order placed with the drone delivery system. It is useful for storing data about the
 * orders so that we can process them.
 */
public class Order {
    public String orderNo;
    public String deliveryDate; // in the format DD-MM-YYYY
    public String costumer; // matriculation number in form s0000000
    public String deliverTo; // in what3words format
    public ArrayList<String> restaurantPlace; // each place is in what3words format
    public ArrayList<String> items;
    public int deliveryCost; // in pence
    public double deliveryDistance;

    public Order(String orderNumber, String date, String matricNo, String threeWord) {
        this.orderNo = orderNumber;
        this.deliveryDate = date;
        this.costumer = matricNo;
        this.deliverTo = threeWord;
        this.items = new ArrayList<String>();
    }



    /**
     * This method adds an item to the items field of the order this method was called on.
     * @param item the name of the item to be added
     */
    public void addItem(String item)  {
        (this.items).add(item);
    }

    /**
     * This method adds a restaurant location to the restaurantPlace array. If the array has not been
     * initialised yet, it also does that.
     * @param w3w the what3words location to be added to the array.
     */
    public void addRestaurant(String w3w) {
        if (this.restaurantPlace == null) {
            this.restaurantPlace = new ArrayList<>();
        }
        (this.restaurantPlace).add(w3w);
    }

    /*
     * This class implements a comparator which allows us to sort orders by cost. This is useful for specifying the
     * order in which the deliveries will be made.
     */
    static class SortByValuePerMove implements Comparator<Order> {
        // Used for sorting in ascending order of
        // roll number
        public int compare(Order a, Order b)
        {

            return (int) (((b.deliveryCost/b.deliveryDistance) - (a.deliveryCost/a.deliveryDistance)));
        }
    }

    /**
     * This method converts on order to a human-readable string. It is useful for debugging and clarity
     * @return order in a human-readable string.
     */
    public String printOrder() {
        return ("orderNo: " + this.orderNo + ", delivery date: " + this.deliveryDate + ", costumer: " + this.costumer
                + ", deliver to: " + this.deliverTo + ", items: "  + (this.items) + ", cost: " + (this.deliveryCost));
    }
}
