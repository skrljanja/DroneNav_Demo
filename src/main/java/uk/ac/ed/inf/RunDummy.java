package uk.ac.ed.inf;

import com.mapbox.geojson.*;
import uk.ac.ed.inf.*;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the class which brings all the functionality of the other classes to
 * construct the  drone path planning process
 */
public class RunDummy {

    // definition of constants
    double APPLETON_LON = -3.186874;
    double APPLETON_LAT = 55.944494;
    int AVAILABLE_MOVES = 1500;
    String FLIGHTPATH_PREPARED_STATEMENT = "insert into flightpath values (?, ?, ?, ?, ?, ?)";
    String DELIVERY_PREPARED_STATEMENT = "insert into deliveries values (?, ?, ?)";


    /**
     * This method runs through the whole path of the drone for a particular day of the year. It creates 2 tables and a geojson file
     * speciyfing details about the path travelled.
     *
     * @param DBmachine  the name of the machine storing the SQL database
     * @param DBport     the port of the machine storing the SQL database
     * @param webMachine the name of the machine of the web server
     * @param webPort    the port at which the web server can be accessed
     * @param day        day of the month, the orders on which are currently being delivered
     * @param month      the orders on which are currently being delivered
     * @param year       the orders on which are currently being delivered
     * @return true if the run was successful.
     */

    public Float run(String DBmachine, String DBport, String webMachine, String webPort, String day, String month, String year) {
        //initialisation of variables
        DerbyAll derby = new DerbyAll(DBmachine, DBport);
        HttpClientAll http = new HttpClientAll(webMachine, webPort);


        LongLat start_pos = new LongLat(APPLETON_LON, APPLETON_LAT);
        LongLat pos = new LongLat(APPLETON_LON, APPLETON_LAT);
        LongLat real_dst;
        String date = year + "-" + month + "-" + day;
        String date_file = day + "-" + month + "-" + year;
        LongLat landM;
        Navigator.navigationReturn navReturn = null;

        ArrayList<Point> points = new ArrayList<>();
        points.add(Point.fromLngLat(APPLETON_LON, APPLETON_LAT));
        int moves = AVAILABLE_MOVES;
        int angle;
        BufferedWriter mapWriter;
        ArrayList<Order> dailyOrders = derby.read.getOrders(date);
        PreparedStatement psFlightpath;
        PreparedStatement psDeliveries;
        List<Geometry> noFlyGeometries = new ArrayList<>();
        List<Geometry> landmarkGeometries = new ArrayList<>();
        int delivered = 0;


        if (! derby.write.MakeDeliveryTable()) {
            System.err.println("making tables was not successful");
        }
        if (! derby.write.makeFlightpathTable()) {
            System.err.println("making tables was not successful");
        }
        if (! GeoJsonUtils.makeGeoJson(date_file)){
            System.err.println("GeoJson file creation was not successful");
        }

        // access the landmarks and no fly zones
        List<Feature> noFly = http.buildings.getNoFlyZone();
        List<Feature> landmarks = http.buildings.getLandmark();
        for (Feature noFlyFeature : noFly) {
            noFlyGeometries.add(noFlyFeature.geometry());
        }
        for (Feature landmark : landmarks) {
            landmarkGeometries.add(landmark.geometry());
        }

        // prepare statements for writing into tables
        try {
            psFlightpath = derby.conn.prepareStatement(
                    FLIGHTPATH_PREPARED_STATEMENT);
            psDeliveries = derby.conn.prepareStatement(
                    DELIVERY_PREPARED_STATEMENT);

        } catch (SQLException e) {
            e.printStackTrace();
            return Float.valueOf(0);
        }

        // update orders with all the information & sort them by cost (descending)
        ArrayList<LongLat> restaurantsLongLat = new ArrayList<>();
        dailyOrders = derby.read.addOrderDetails(dailyOrders);
        for (Order order : dailyOrders) {
            http.menus.updateOrder(order);
            for (String restaurant : order.restaurantPlace) {
                restaurantsLongLat.add(http.words.w3wToLongLat(restaurant));
            }
            order.deliveryDistance = Navigator.getDeliveryDistance(restaurantsLongLat, http.words.w3wToLongLat(order.deliverTo), landmarkGeometries, noFlyGeometries);
        }
        dailyOrders = DeliveryOrder.sortDeliveryOrder(dailyOrders, http.words);

        // start running through the delivery process
        for (Order order : dailyOrders) {
            // if the drone has a sufficient battery left, it should start completing the next order.
            if (moves > 250)  {
                // find and move to the location of the restaurant (for every restaurant)
                int n = (order.restaurantPlace).size();
                for (int x = 0 ; x < n ; x++ ) {
                    // find and move to the restaurant location
                    real_dst = http.words.w3wToLongLat(order.restaurantPlace.get(x));
                    if (real_dst.isConfined()) {
                        navReturn = Navigator.navigateLocation(pos, real_dst, landmarkGeometries, noFlyGeometries, psFlightpath, order.orderNo);
                        pos = navReturn.newPosition;
                        landM = navReturn.landmark;
                        moves = moves - navReturn.moves;
                        //mark the points visited (maybe landmark, definitely restaurant location) into GeoJson file
                        if (! landM.closeTo(pos)) {
                            points.add(Point.fromLngLat(landM.longitude, landM.latitude));
                        }
                        points.add(Point.fromLngLat(pos.longitude, pos.latitude));
                    } else {
                        System.err.println("encountered destination outside of confinement area!");
                    }
                }

                // find and move to the delivery location
                real_dst = http.words.w3wToLongLat(order.deliverTo);

                if (real_dst.isConfined()) {
                    navReturn = Navigator.navigateLocation(pos, real_dst, landmarkGeometries, noFlyGeometries, psFlightpath, order.orderNo);
                    pos = navReturn.newPosition;
                    landM = navReturn.landmark;
                    moves = moves - navReturn.moves;
                    //mark the points visited (maybe landmark, definitely delivery location) into GeoJson file
                    if (! landM.closeTo(pos)) {
                        points.add(Point.fromLngLat(landM.longitude, landM.latitude));
                    }
                    points.add(Point.fromLngLat(pos.longitude, pos.latitude));
                    delivered += 1;
                } else {
                    System.err.println("encountered destination outside of confinement area!");
                }


                // enter the delivery made into the deliveries table
                if (! DerbyWrite.noteDelivery(psDeliveries, order.orderNo, order.deliverTo, order.deliveryCost)) {
                    System.err.println("could not enter delivery into the table, orderNo: " + order.orderNo);
                    return Float.valueOf(0);
                }
            }
        }

        // if all the orders are completed, or the moves are running too low, navigate back to initial position
        System.out.println("moves remaning before navigating to appleton: " + moves);
        navReturn = Navigator.navigateLocation(pos, start_pos, landmarkGeometries, noFlyGeometries, psFlightpath, "none");
        pos = navReturn.newPosition;
        landM = navReturn.landmark;
        moves = moves - navReturn.moves;
        //mark the current position as a point visited on the geojson file
        if (! landM.closeTo(pos)) {
            points.add(Point.fromLngLat(landM.longitude, landM.latitude));
        }
        points.add(Point.fromLngLat(pos.longitude, pos.latitude));


        if (moves < 0) {
            System.err.println("too many moves were made, the drone ran out of battery, date: " + date);
            return Float.valueOf(0);
        }

        //write this part onto the map too
        points.add(Point.fromLngLat(APPLETON_LON, APPLETON_LAT));



        LineString line = LineString.fromLngLats(points);
        Feature path = Feature.fromGeometry(line);
        List<Feature> features = new ArrayList<>();
        features.add(path);
        features.addAll(noFly);
        features.addAll(landmarks);

        FeatureCollection fc = FeatureCollection.fromFeatures(features);
        boolean success =  (GeoJsonUtils.writeToGeoJson(date_file, fc));
        System.out.println(delivered);
        System.out.println(dailyOrders.size());
        System.out.println(Float.valueOf((delivered/dailyOrders.size())));
        return Float.valueOf((Float.valueOf(delivered)/Float.valueOf(dailyOrders.size())));

    }
}

