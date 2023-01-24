package uk.ac.ed.inf;

/**
 * This is the class used for representing points on a map as longitude-latitude pairs. It contains some geometric functions
 * which are needed for the rest of the system to operate.
 */
public class LongLat {
    public double longitude;
    public double latitude;

    // definition of necessary constants
    private static final double MIN_LONGITUDE = -3.192473;
    private static final double MAX_LONGITUDE = -3.184319;
    private static final double MIN_LATITUDE = 55.942617;
    private static final double MAX_LATITUDE = 55.946233;
    private static final double MOVEMENT_UNIT = 0.00015;
    private static final int JUNK_ANGLE = -999;


    public LongLat(double lon, double lat) {
        longitude = lon;
        latitude = lat;
    }

    /**
     * This method checks whether a longitude latitude pair is within the drone confinement area.
     * @return true if the coordinates are within drone confinement area
     */
    public boolean isConfined() {
        if (!(this.longitude > MIN_LONGITUDE && this.longitude < MAX_LONGITUDE))  {
            return false;
        } else return this.latitude > MIN_LATITUDE && this.longitude < MAX_LATITUDE;
    }

    /**
     * This method calculates the distance between 2 LongLat class points. It returns the distance as a
     * double with degrees as the unit.
     * @param point a point given as instance of the LongLat class
     * @return distance between input point and point the method was called on
     */
    public double distanceTo(LongLat point) {
        return Math.sqrt (Math.pow((this.longitude -point.longitude),2) +
                             Math.pow((this.latitude -point.latitude),2));
    }

    /**
     * This method checks if a given point is within an acceptable distance to be considered "close to"
     * the point the method is called on. The acceptable distance is defined as 0.00015 degrees.
     * @param point a point given as an instance of the LongLat class
     * @return boolean that is true if the distance between the points is strictly less then 0.00015
     */
    public boolean closeTo(LongLat point) {
        return this.distanceTo(point) < MOVEMENT_UNIT;
    }

    /**
     * This method takes an angle in degrees (where East is defined as 0) and returns the new position after moving
     * 0.00015 degrees in that direction.
     * @param angleDeg is the angle (in degrees) at which the drone is moving. An angle of -999 is the "junk angle" taken to
     *                mean staying in place.
     * @return the new position of the drone after it has moved as an instance of LongLat
     */
    public LongLat nextPosition(int angleDeg) {
        if (angleDeg ==  JUNK_ANGLE) {
            return new LongLat(this.longitude, this.latitude);
        }
        double angleRad = Math.toRadians(angleDeg);
        double newLong = this.longitude + (Math.cos(angleRad) * MOVEMENT_UNIT);
        double newLat = this.latitude + (Math.sin(angleRad) * MOVEMENT_UNIT);
        return new LongLat(newLong, newLat);
    }


    /**
     * This method takes another LongLat as an argument. It then calculates the angle from the LongLat this method was called on
     * (the initial position) to the LongLat in the argument (the destination).
     * Effectively, this tells us at what angle the drone needs to move to reach the destination from its position.
     * @param dst of type LongLat, the destination which we are trying to reach.
     * @return angle between this and dst, in degrees rounded to 10.
     */
    public int findAngle(LongLat dst)  {
        double opp = dst.latitude - this.latitude;
        double adj = dst.longitude - this.longitude;

        double rad = Math.atan2(opp, adj);
        int deg = (int) (Math.toDegrees(rad));
        return deg;
    }
}