package uk.ac.ed.inf;

import java.sql.*;
import java.util.ArrayList;

/**
 * This is the class used for reading the SQL database. It is always already given a connection to the database when
 * it is instantiated. It has the methods that read the tables and store their data in the appropriate structures.
 */
public class DerbyRead {
    public Connection conn;

    public DerbyRead(Connection connect) {
        conn = connect;
    }

    /**
     * This method connects to the derby database, and retrieves all entries from the orders table, for which
     * the date field is equal to the argument string.
     * It returns an array of the orders corresponding to the date. The fields orderNo (order number),
     * deliverTo (delivery location in what3words format) and customer (the matriculation number of the
     * customer) are filled out, as they are the ones available in the orders table.
     * @param date in the format DD-MM-YYYY
     * @return dailyOrders an array of all the orders which correspond to the input date.
     */
    public ArrayList<Order> getOrders (String date) {

        String orderNo = null;
        String deliverTo = null;
        String customer = null;
        final String ordersQuery =
                "select * from orders where deliveryDate=(?)";
        PreparedStatement psOrdersQuery = null;
        try {
            psOrdersQuery =
                    this.conn.prepareStatement(ordersQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            psOrdersQuery.setString(1, date);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ArrayList<Order> orderList = new ArrayList<>();
        ResultSet rs = null;
        try {
            rs = psOrdersQuery.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }


            while (true) {
            try {
                if (!rs.next()) break;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                orderNo = rs.getString("orderNo");
                deliverTo = rs.getString("deliverTo");
                customer = rs.getString("customer");

            } catch (SQLException e) {
                e.printStackTrace();
            }

            orderList.add(new Order(orderNo, date, customer, deliverTo));

        }
        return orderList;
    }

    /**
     * This method recieves an array of orders as an argument(the orders must have the orderNo field filled out).
     * It connects to the derby database, finds the orders in the orderDetail tables, and retrieves the items which were
     * ordered. It places them in the items field of the order objects. Note that the field cost and restaurantPlaces
     * remain empty for now.
     * @param orderList - arrayList of Order objects, the OrderNo of which must be filled out.
     * @return orderList - the same array of Order objects, but with the items field of each of the Order objects updated.
     */
    public ArrayList<Order> addOrderDetails(ArrayList<Order> orderList) {

        String item_placeholder = "";
        for (Order order : orderList) {
            String orderNo = order.orderNo;

            final String detailsQuery =
                    "select * from orderDetails where orderNo=(?)";
            PreparedStatement psOrdersQuery = null;
            try {
                psOrdersQuery =
                        this.conn.prepareStatement(detailsQuery);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                psOrdersQuery.setString(1, orderNo);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            ResultSet rs = null;
            try {
                rs = psOrdersQuery.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                if (!rs.next()) break;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                item_placeholder = rs.getString("item");
                order.addItem(item_placeholder);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return orderList;
    }
}
