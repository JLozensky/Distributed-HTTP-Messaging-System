package Client1;



import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.LocalDateTime;

import java.util.concurrent.CountDownLatch;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import SharedClientClasses.*;

/**
 * Client implementation for part one of assignment one
 */

public class ClientOne extends AbstractClient {

    private int numThreads;

    /**
     * Constructor matching super
     */
    public ClientOne(int numThreads, int numSkiers, int numLifts, int numRuns, String ipAddress, String port) {
        super(numThreads, numSkiers, numLifts,numRuns,ipAddress,port);
        this.numThreads = numThreads;
    }

    /**
     * Makes an instance of the runnable object that is given to a different thread to do a set of POST requests
     * @param skierStart Skier id range start value
     * @param skierEnd Skier id range end value
     * @param startTime Time range start value
     * @param endTime Time range end value
     * @param numPosts Number of posts the thread needs to make
     * @param gates an object containing any relevant gates for the thread to wait on and/or countdown
     * @return A single runnable object
     */
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

    public int getNumThreads(){return this.numThreads;}

    /**
     * Measures latency for a single thread using wall time
     * @param numPosts the number of posts to run
     * @return the average latency across all posts in ms
     */
    protected float singleThreadLatencyCalc(int numPosts) {

        // prep Objects needed for the test
        CountDownLatch gate = new CountDownLatch(1);
        Gates gates = new Gates(null, null, gate);

        // make starting timestamp
        LocalDateTime startTime = LocalDateTime.now();

        // run the test on its own thread
        Thread thread = new Thread(this.makeLiftPoster(1,100,91, 330,numPosts,gates));
        thread.start();

        // wait for the thread to finish its POST requests
        try {
            gate.await();
        } catch (InterruptedException e) {
            // no need to do anything as we have the count of requests that went through
        }

        // take ending timestamp
        LocalDateTime endTime = LocalDateTime.now();

        // get the total requests made from the AtomicIntegers and reset them for the main tests
        int requestsCompleted = super.getSuccessfulTotal() + super.getUnsuccessfulTotal();
        super.resetAtomicCounters();

        // return the average time in milliseconds taken to complete one POST request
        return (float) Duration.between(startTime,endTime).toMillis() / requestsCompleted;

    }

    /**
     * This static method drives a single run of a ClientOne instance, this would normally be in main but I split it
     * out so the assignment output requirements logic is able to run multiple tests from main
     * @param args the command line arguments that provide a Client Object with its parameters
     * @param testLatency a boolean indicating if latency should be tested before running the three test phases
     * @return The length of time taken to run the 3 phases in seconds
     */
    public static long singleTestDriver(String args[], boolean testLatency) {

        // create client from provided args
        ClientOne client = (ClientOne) ArgParsingUtility.makeClient(args, 1);
        float latency = 0f;

        // If testing latency is true test the latency and print the results
        if (testLatency) {
            int latencyRequests = 1000;
            latency = client.singleThreadLatencyCalc(latencyRequests);
            System.out.printf("Average Latency in ms (single thread %d requests): %.1f\n", latencyRequests, latency);
        }

        // print which number of threads the results will be from
        System.out.println(client.getNumThreads() + "-thread test results: \n");

        // get the gate that only releases once all threads finish
        CountDownLatch finalGate = client.getFinalGate();

        // take starting timestamp
        LocalDateTime startTime = LocalDateTime.now();

        // run all three phases, there are gates within the client that will control thread timing
        client.startPhaseOne();
        client.startPhaseTwo();
        client.startPhaseThree();

        // wait for all threads to finish
        try {
            finalGate.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // take ending timestamp
        LocalDateTime endTime = LocalDateTime.now();

        // find the duration of the test in seconds
        long duration = Duration.between(startTime, endTime).toSeconds();

        // calculate the average number of POST requests completed per second
        float requestsPerSecond = (float)
            (client.getSuccessfulTotal() + client.getUnsuccessfulTotal()) / duration;

        // print results
        System.out.println("Start time was: " + client.makeTime(startTime) + " | End time was: " + client.makeTime(endTime));
        System.out.println("Total time elapsed in seconds: " + duration);
        System.out.println("successful total: " + client.getSuccessfulTotal());
        System.out.println("unsuccessful total: " + client.getUnsuccessfulTotal());
        System.out.printf("requests per second: %.1f\n\n\n", requestsPerSecond);

        //
        client.threadPool.close();

        // returning duration is used for graphing purposes per the assignment specs
        return duration;
    }


    public static void main(String args[]) {

        // if not running the comparison for the assignment, do a single run
        if (! args[0].equals("assignmentMode")) {
            System.out.printf("time taken in milliseconds: %d", singleTestDriver(args,false));
        } else {

            boolean testLatency = true;

            // here we do the four runs for the assignment and put together the chart
            // define values for args for each run
            final String hardcodedIP = "localhost";
            final String SKIER_NUM = "20000";
            final String LIFT_NUM = "40";
            final String PORT_NUM = "8080";
            final int[] THREAD_NUMS = {32,64,128,256};

            // Set up charting tool
            BarChartMaker bcm = new BarChartMaker("ClientOne Thread Count Comparison", "NumThreads","Time Taken");

            // for each thread num to test
            for (int threadNum:THREAD_NUMS) {

                // create an args string as if the commands were passed in via commandline
                String[] arguments = {
                    "-t", String.valueOf(threadNum),
                    "-s", SKIER_NUM,
                    "-l", LIFT_NUM,
                    "-p", PORT_NUM,
                    "-ip", hardcodedIP
                };



                // add the datapoint of time taken to the graph
                bcm.addDatapoint(singleTestDriver(arguments, testLatency),"CompletionTime", String.valueOf(threadNum));

                // set to false so we only test latency once
                testLatency = false;
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

