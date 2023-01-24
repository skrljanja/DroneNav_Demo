package uk.ac.ed.inf;

import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

import java.awt.geom.Line2D;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
/**
 * This is the class used for most of the actual drone path planning. It also contains the geometric functions necessary
 * for the planning.
 */
public class Navigator {

    public static class navigationReturn {
        int moves;
        LongLat landmark;
        LongLat newPosition;

        public navigationReturn(int move, LongLat landm, LongLat newPos)  {
            moves = move;
            landmark = landm;
            newPosition = newPos;
        }
    }

    private Navigator() { }

    /**
     * This method takes a StringLine object and a Polygon pbject. It transforms the line into 2D line object, and the
     * polygon into a set of 2D line objects. It checks the intersection of the line with all the 2D lines of the polygon. If none
     * of them intersect, it returns false. Otherwise, true.
     * @param line a StringLine object, representing the straight path between points A and B
     * @param polygon a Polygon object representing a no-fly zone.
     * @return true if the line and polygon intersect. False otherwise.
     */
    public static boolean intersect(LineString line, Polygon polygon) {
        boolean result = true;
        List<Point> points = line.coordinates();
        Line2D.Double poly_line;
        Line2D.Double path = new Line2D.Double(points.get(0).longitude(), points.get(0).latitude(),
                points.get(1).longitude(), points.get(1).latitude());
        List<List<Point>> lines = polygon.coordinates();
        List<Point> poly_points = lines.get(0);

        for (int i = 0; i < poly_points.size() - 1; i++) {
            poly_line = new Line2D.Double(poly_points.get(i).longitude(), poly_points.get(i).latitude(),
                    poly_points.get(i+1).longitude(), poly_points.get(i+1).latitude());
            if (path.intersectsLine(poly_line)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method is used to avoid no-fly zones. It gets a desired position to be reached and first checks if that position
     * can be reached via a straight line. If not, then it inspects the landmark to find one through which the destination can be reached.
     * @param landmarkGeometries a list of all the landmarks (Points to be used for avoiding no-fly zones
     * @param noFlyGeometries a list of all the polygons forming the no-fly zone
     * @param pos the start position of the drone
     * @param dst the desired end position of the drone
     * @return a list of orders containing the same orders as dailyOrders, but sorted differently.
     */
    public static LongLat findDestination(List<Geometry> landmarkGeometries, List<Geometry> noFlyGeometries, LongLat pos, LongLat dst) {
        Point pos_p = Point.fromLngLat(pos.longitude, pos.latitude);
        Point dst_p = Point.fromLngLat(dst.longitude, dst.latitude);
        List<Point> points1 = Arrays.asList(pos_p, dst_p);
        LineString path1 = LineString.fromLngLats(points1);
        List<Point> points2 = Arrays.asList(pos_p, dst_p);
        LineString path2 = LineString.fromLngLats(points2);
        LongLat result = new LongLat(0, 0);
        boolean intersectsNone = true;

        for (Geometry polygon : noFlyGeometries) {
            if (intersect(path1, (Polygon) polygon)) {
                intersectsNone = intersectsNone && false;
            }
            for (Geometry landmark : landmarkGeometries) {
                points1 = Arrays.asList(pos_p, (Point) landmark);
                path1 = LineString.fromLngLats(points1);
                points2 = Arrays.asList((Point) landmark, dst_p);
                path2 = LineString.fromLngLats(points2);
                if (!(intersect(path1, (Polygon) polygon)) && !(intersect(path2, (Polygon) polygon))) {
                    result.longitude = ((Point) landmark).coordinates().get(0);
                    result.latitude = ((Point) landmark).coordinates().get(1);
                }
            }
        }
        if (intersectsNone) {
            return dst;
        }
        return result;
    }

    /**
     * This method is used to navigate the drone from its current position to the desired position. It uses the
     * findDestination method to plan its path, and then makes all the individual moves leading it there,
     * noting each one in the flightpath table. It returns the number of moves made, the points visited, and the new position
     * @param landmarkGeometries a list of all the landmarks (Points to be used for avoiding no-fly zones)
     * @param noFlyGeometries a list of all the polygons forming the no-fly zone
     * @param pos the start position of the drone
     * @param dst the desired end position of the drone
     * @param psFlightpath the prepared statement for writing moves into the flightpath table
     * @param orderNo the order number of the order being delivered.
     * @return the number of moves made, the points visited, and the new position, package in a navigateReturn object
     */
    public static navigationReturn navigateLocation(LongLat pos, LongLat dst, List<Geometry> landmarkGeometries, List<Geometry> noFlyGeometries, PreparedStatement psFlightpath, String orderNo) {
        LongLat first_dst = findDestination(landmarkGeometries, noFlyGeometries, pos, dst);
        int angle = pos.findAngle(first_dst);
        int moves = 0;
        LongLat landmark = first_dst;

        // if we need to navigate to a landmark first, this is where its done
        if (! first_dst.closeTo(dst)) {
            while (! pos.closeTo(first_dst)) {
                if (DerbyWrite.noteMove(psFlightpath, orderNo, pos, angle, pos.nextPosition(angle))) {
                    pos = pos.nextPosition(angle);
                    moves = moves + 1;
                } else {
                    System.err.println("failed while making orders. Order number: " + orderNo);
                }
            }
            landmark = pos;
        }

        angle = pos.findAngle(dst);
        // until the restaurant is reached, move towards it
        while (! pos.closeTo(dst)) {
            if (DerbyWrite.noteMove(psFlightpath, orderNo, pos, angle, pos.nextPosition(angle))) {
                moves = moves + 1;
                pos = pos.nextPosition(angle);
            } else {
                System.err.println("could not move towards restaurant, order number: " + orderNo);
            }
        }

        // once we have reached the destination, hover for one move
        if (DerbyWrite.noteMove(psFlightpath, orderNo, pos, 999, pos)) {
            moves = moves + 1;
        } else {
            System.err.println("could not hover over restaurant, order number: " + orderNo);
        }
        return (new navigationReturn(moves, landmark, pos));
    }

    /**
     * This method is used to calculate the distance to be travelled for a specific delivery. It does so using the
     * findDestination method.
     * @param landmarkGeometries a list of all the landmarks (Points to be used for avoiding no-fly zones)
     * @param noFlyGeometries a list of all the polygons forming the no-fly zone
     * @param restaurants the list of restaurants to be visited in the order
     * @param deliveryLoc the delivery location of the order
     * @return the distance to be travelled for a specific delivery
     */
        public static double getDeliveryDistance(ArrayList<LongLat> restaurants, LongLat deliveryLoc, List<Geometry> landmarkGeometries, List<Geometry> noFlyGeometries) {
            LongLat first_landmark;
            LongLat second_landmark;
            double distance = 0;
            if (restaurants.size() == 2) {
                first_landmark = findDestination(landmarkGeometries, noFlyGeometries,restaurants.get(0), restaurants.get(1));
                second_landmark = findDestination(landmarkGeometries, noFlyGeometries, restaurants.get(1), deliveryLoc);
                distance = restaurants.get(0).distanceTo(first_landmark)
                        + first_landmark.distanceTo(restaurants.get(1))
                        + restaurants.get(1).distanceTo(second_landmark)
                        + second_landmark.distanceTo(deliveryLoc);
            } else {
                first_landmark = findDestination(landmarkGeometries, noFlyGeometries, restaurants.get(0), deliveryLoc);
                distance = restaurants.get(0).distanceTo(first_landmark) + first_landmark.distanceTo(deliveryLoc);
                return distance;
            }
            return distance;
        }
}
