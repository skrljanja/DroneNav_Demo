package uk.ac.ed.inf;


import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;


/**
 * This is the class used for connecting to the buildings part of the web server. It is used for obtaining the no-fly
 * zones and the landmarks.
 */
public class HttpBuildings {
    public String machine;
    public String port;

    public HttpBuildings(String machineName, String portName) {
        machine = machineName;
        port = portName;
    }

    /**
     * This method creates a geoJson file, of the name "drone-DD-MM-YYY.geojson" in the top level directory. If a file with the
     * na,me already exists, it is first deleted.
     * @param date in DD-MM-YYYY format
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
     * This method retrieves the geoJson file containing the no fly zones (contained on web server). It converts the
     * geoJson file into a list of features and returns it.
     * @return the no fly zones as a list of features (these will be polygons)
     */
    public List<Feature> getNoFlyZone() {
        String urlString = "http://" + this.machine + ":" + this.port + "/buildings/no-fly-zones.geojson";
        HttpResponse<String> response = null;
        List<Feature> noFly = null;

        // this part of code handles the Http request and response
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .build();
        try {
            response = HttpClientAll.client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 404) {
                System.out.println("Error 404:" + urlString + " is not found. Check for typos.");
                throw new FileNotFoundException("Error 404:" + urlString + " is not found. Check for typos.");
            }
        } catch (Exception e)  {
            System.out.println("This menu database cannot be accessed. Check that the machine and port fields are correct.");
        }
        assert response != null;
        if (response.statusCode() == 200) { // the response.body() is only used if the response.statusCode tells us the request was valid (200)
            noFly = (FeatureCollection.fromJson(response.body())).features();
        }
        return noFly;
    }

    /**
     * This method retrieves the geoJson file containing "landmarks" (contained on web server). The landmarks are
     * points the drone uses to avoid passing through no fly zones. It converts the
     * geoJson file into a list of features and returns it.
     * @return the landmarks as a list of features (these will be points)
     */
    public List<Feature> getLandmark() {
        String urlString = "http://" + this.machine + ":" + this.port + "/buildings/landmarks.geojson";
        HttpResponse<String> response = null;
        List<Feature> landmarks = null;

        // this part of code handles the Http request and response
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .build();
        try {
            response = HttpClientAll.client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 404) {
                System.err.println("Error 404:" + urlString + " is not found. Check for typos.");
                throw new FileNotFoundException("Error 404:" + urlString + " is not found. Check for typos.");
            }
        } catch (Exception e)  {
            System.err.println("This menu database cannot be accessed. Check that the machine and port fields are correct.");
        }
        assert response != null;
        if (response.statusCode() == 200) { // the response.body() is only used if the response.statusCode tells us the request was valid (200)
            landmarks= (FeatureCollection.fromJson(response.body())).features();
        }
        return landmarks;
    }
}

