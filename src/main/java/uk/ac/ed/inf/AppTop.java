package uk.ac.ed.inf;


import org.apache.commons.lang.time.StopWatch;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class is the main class of the uber JAR file. It interprets the arguments given and then runs the path using the
 * RunPath class
 */
public class AppTop {

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
        List<Float> ratios = new ArrayList<>();
        List<Long> times = new ArrayList<>();

        String month = new String();
        String day = new String();
        Float ratio_sum = Float.valueOf(0);
        Float ratio;
        int days = 0;

        String year = "2023";
        for (int m = 7;m <= 10; m++ ) {
            for (int d = 1;d <= 4; d++ ) {
                days += 1;
                month = String.format("%02d", m);
                day = String.format("%02d", d);

                StopWatch stopwatch = new StopWatch();
                stopwatch.start();

                RunDummy runner = new RunDummy();
                ratio = runner.run(db_machine, db_port, web_machine, web_port, day, month, year);
                ratios.add(ratio);
                ratio_sum = ratio_sum + ratio;

                stopwatch.stop();
                Long timeTaken = stopwatch.getTime();

                times.add(timeTaken);
            }
        }

        Float avg_ratio = ratio_sum/days;
        Long longest_time = Collections.max(times);


        System.out.println("average deliveries ratio: " + avg_ratio);
        System.out.println("worst time: " + longest_time);



    }
}