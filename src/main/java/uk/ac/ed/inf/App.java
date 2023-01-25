package uk.ac.ed.inf;


import org.apache.commons.lang.time.StopWatch;

/**
 * This class is the main class of the uber JAR file. It interprets the arguments given and then runs the path using the
 * RunPath class
 */
public class App {

    public static void main( String[] args ) {

        String db_machine = "localhost";
        String web_machine = "localhost";
        /**String db_port = args[4];
        String web_port = args[3];

        String year = args[2];
        String month = args[1];
        String day = args[0];*/

        String db_port = "9876";
        String web_port = "9898";

        String year = "2022";
        String month = "01";
        String day = "01";

        String date = year + "-" + month + "-" + day;
        System.out.println("Planning the drone's path for the day: " + date);
        StopWatch stopwatch = new StopWatch();
        stopwatch.start();
        RunPath runner = new RunPath();
        if (! runner.run(db_machine, db_port, web_machine, web_port, day, month, year)) {
            System.err.println("something went wrong during path");
        }
        System.out.println("done!");

        stopwatch.stop();
        String timeTaken = stopwatch.toString();
        // DerbyAll db = new DerbyAll("localhost", "9876");
        System.out.println(timeTaken);


    }
}