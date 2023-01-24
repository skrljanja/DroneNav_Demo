package uk.ac.ed.inf;

import java.sql.*;

/**
 * This is the class used for writing onto the SQL database. It is always already given a connection to the database when
 * it is instantiated. It has the methods that use said connection to create tables and write onto the database.
 */
public class DerbyWrite {
    public Connection conn;

    String FLIGHTPATH_CREATE_TABLE = "create table flightpath" + "" +
            "(orderNo char(8)," +
            "fromLongitude double," +
            "fromLatitude double," +
            "angle integer," +
            "toLongitude double," +
            "toLatitude double)";
    String DELIVERY_CREATE_TABLE = "create table deliveries" +
            "(orderNo char(8)," +
            "deliveredTo varchar(19)," +
            "costInPence int)";

    public DerbyWrite(Connection connection) {

        conn = connection;
    }


    /**
     * This method makes the table deliveries in the derby database - this is the table that will store information about all the
     * completed orders. It also deletes the table if it existed beforehand. It returns true if the table was created
     * successfully.
     *  @return true if table was successfully made with no exceptions or errors.
     */
    public boolean MakeDeliveryTable() {
        Statement statement;
        ResultSet resultSet;
        DatabaseMetaData databaseMetadata;

        try {
            statement = this.conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        try {
            databaseMetadata = this.conn.getMetaData();
            resultSet =
                    databaseMetadata.getTables(null, null, "DELIVERIES", null);
            if (resultSet.next()) {
                statement.execute("drop table deliveries");
            }
            statement.execute(DELIVERY_CREATE_TABLE);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;

    }

    /**
     * This method makes the table flightpath in the derby database - this table dtores information on every move
     * made by the drone. It also deletes the table if it existed beforehand. It returns true if the table was created
     * successfully.
     * @return true if table was successfully created with no exceptions or errors.
     */
    public boolean makeFlightpathTable() {
        Statement statement;
        ResultSet resultSet;
        DatabaseMetaData databaseMetadata;

        try {
            // conn = DriverManager.getConnection(jdbcString);
            statement = this.conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        try {
            databaseMetadata = this.conn.getMetaData();
            resultSet =
                    databaseMetadata.getTables(null, null, "FLIGHTPATH", null);
            if (resultSet.next()) {
                statement.execute("drop table flightpath");
            }

            statement.execute(FLIGHTPATH_CREATE_TABLE);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * This method notes a specified move in the flightpath table. It is given the prepared statement and all the values
     * to input into the statement. It executes the statement, thereby adding a new filled out row to the flightpath table.
     * @param psFlightpath the prepared statement to be executed
     * @param orderNo String to be written into order number field
     * @param pos LongLat to be written into initial position field (converted to String first)
     * @param angle int to be written into angle field (converted to String first)
     * @param next_pos LongLat to be written into final position field (converted to String first)
     * @return true if move was written successfully.
     */
    public static boolean noteMove(PreparedStatement psFlightpath, String orderNo, LongLat pos, int angle, LongLat next_pos) {
        try {
            psFlightpath.setString(1, orderNo);
            psFlightpath.setString(2, String.valueOf(pos.longitude));
            psFlightpath.setString(3, String.valueOf(pos.latitude));
            psFlightpath.setString(4, String.valueOf(angle));
            psFlightpath.setString(5, String.valueOf(next_pos.longitude));
            psFlightpath.setString(6, String.valueOf(next_pos.latitude));

            psFlightpath.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * This method notes a specified delivery made in the deliveries table. It is given the prepared statement and all the values
     * to input into the statement. It executes the statement, thereby adding a new filled out row to the deliveries table.
     * @param psDeliveries the prepared statement to be executed
     * @param orderNo String to be written into order number field
     * @param deliverTo String to be written into the "deliver to" field
     * @param deliveryCost int to be written into cost field (converted to String first)
     * @return true if move was written successfully.
     */
    public static boolean noteDelivery(PreparedStatement psDeliveries, String orderNo, String deliverTo, int deliveryCost) {
        try {
            psDeliveries.setString(1, orderNo);
            psDeliveries.setString(2, deliverTo);
            psDeliveries.setString(3, String.valueOf(deliveryCost));
            psDeliveries.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
