package uk.ac.ed.inf;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This is the class used for connecting to the SQL database. It ensures that the DerbyRead and DerbyWrite objects are
 * instantiated with an already existing connection.
 */
public class DerbyAll {
    DerbyRead read;
    DerbyWrite write;
    Connection conn;

    public DerbyAll(String machine, String port) {
        try {
            conn = DriverManager.getConnection("jdbc:derby://" + machine + ":" + port + "/" + "derbyDB");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        read = new DerbyRead(conn);
        write = new DerbyWrite(conn);
    }
}
