package Client1;



import SharedClientClasses.AbstractClient;
import SharedClientClasses.ArgParsingUtility;
import SharedClientClasses.BarChartMaker;
import SharedClientClasses.Gates;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

public class ClientOne extends AbstractClient {


    public ClientOne(int numThreads, int numSkiers, int numLifts, int numRuns, String ipAddress, String port) {
        super(numThreads, numSkiers, numLifts,numRuns,ipAddress,port);
    }

        protected ClientOneLiftPostingRunnable makeLiftPoster(int skierStart, int skierEnd, int startTime, int endTime,
        int numPosts, Gates gates) {
        return new ClientOneLiftPostingRunnable
       (

           // skierID range
           skierStart, skierEnd,

           // start and end time in minutes
           startTime, endTime,

           // the number of posts to make and the max lift id
           numPosts, super.getNumLifts(),

           // the atomic integers for tracking successful and unsuccessful posts
           super.getSuccessfulRequests(), super.getUnsuccessfulRequests(),

           // Gates object with relevant CountDownLatches
           gates,

           // ipAddress and port to send the requests to
           super.client, super.ipAddress, super.port,  super.runLocally
       );
    }


    public static long singleTestDriver(String args[]) {

        // create client from provided args
        ClientOne client = (ClientOne) ArgParsingUtility.makeClient(args, 1);

        // get the gate that only releases once all threads finish
        CountDownLatch finalGate = client.getFinalGate();

        // take starting timestamp
        LocalDateTime startTime = LocalDateTime.now();

        // run all three phases, there are gates within the client that will control thread timing
        client.startPhaseOne();
        client.startPhaseTwo();
        client.startPhaseThree();

        try {
            finalGate.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // take ending timestamp
        LocalDateTime endTime = LocalDateTime.now();

        long duration = Duration.between(startTime, endTime).toMillis();

        float requestsPerSecond =
            (client.getSuccessfulTotal() + client.getUnsuccessfulTotal()) / (duration / 1000f);
        System.out.println("Start time was: " + client.makeTime(startTime) + " | End time was: " + client.makeTime(endTime));
        System.out.println("successful total: " + client.getSuccessfulTotal());
        System.out.println("unsuccessful total: " + client.getUnsuccessfulTotal());
        System.out.printf("requests per second: %.1f\n\n\n", requestsPerSecond);


        return duration;
    }


    public static void main(String args[]) {

        // if not running the comparison for the assignment, do a single run
        if (! args[0].equals("assignmentMode")) {
            System.out.printf("time taken in milliseconds: %d", singleTestDriver(args));
        } else {

            // here we do the four runs for the assignment and put together the chart
            final String hardcodedIP = "34.230.45.118";
            final String SKIER_NUM = "200";
            final String LIFT_NUM = "40";
            final String PORT_NUM = "8080";
            final int[] THREAD_NUMS = {32, 64, 128, 256};


            BarChartMaker bcm = new BarChartMaker("ClientOne Thread Count Comparison", "NumThreads","Time Taken");



            for (int threadNum:THREAD_NUMS) {
                String[] arguments = {
                    "-t", String.valueOf(threadNum),
                    "-s", SKIER_NUM,
                    "-l", LIFT_NUM,
                    "-p", PORT_NUM,
                    "-ip", hardcodedIP
                };
                System.out.println(threadNum + "-thread test results: \n");
                bcm.addDatapoint(singleTestDriver(arguments),"CompletionTime", String.valueOf(threadNum));
            }

            try {
                SwingUtilities.invokeAndWait(()-> {
                    bcm.makeChart();
                    bcm.setSize(800, 400);
                    bcm.setLocationRelativeTo(null);
                    bcm.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                    bcm.setVisible(true);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

    }




}

