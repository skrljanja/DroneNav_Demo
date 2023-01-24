package uk.ac.ed.inf;

import com.mapbox.geojson.FeatureCollection;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This is the class used for creating and editing the geojson files that store the path for specific days.
 */
public class GeoJsonUtils {

    /**
     * This method creates a geoJson file, of the name "drone-DD-MM-YYY.geojson" in the top level directory. If a file with the
     * na,me already exists, it is first deleted.
     * @param date the date which this geojson file will correspond to
     * @return true if the file was successfully created.
     */
    public static boolean makeGeoJson(String date) {
        File map = new File("drone-" + date + ".geojson");
        map.delete();
        try {
            if ( ! map.createNewFile()) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * This method is given a feature collection and a date string. It writes the feature collection onto the geojson
     * file corresponding to that date.
     * @param fc the FeatureCollection to be written into the geojson file
     * @param date the date which this feature collection corresponds to
     * @return true if the writing was performed succesffuly
     */
    public static boolean writeToGeoJson(String date, FeatureCollection fc) {
        try {
            BufferedWriter mapWriter = new BufferedWriter(new FileWriter("drone-" + date + ".geojson", true));
            mapWriter.write(fc.toJson());
            mapWriter.close();
        } catch (IOException e) {
            System.err.println("could not write onto geoJson file for " + date + "!!!");
            return false;
        }
        return true;
    }

}
