package A1.Client2;

import A1.SharedClientClasses.AbstractClient;
import A1.SharedClientClasses.ArgParsingUtility;
import A1.SharedClientClasses.BarChartMaker;
import A1.SharedClientClasses.Gates;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class ClientTwo extends AbstractClient {


    private ConcurrentLinkedQueue<RequestData>  requestDataRepository;


    public ClientTwo(int numThreads, int numSkiers, int numLifts, int numRuns, String ipAddress, String port){

        super(numThreads, numSkiers, numLifts, numRuns, ipAddress, port);

        this.requestDataRepository = new ConcurrentLinkedQueue<>();
    }

    /**
     * Makes numPost requests from a single thread to test server latency
     * @param numPosts number of requests to make
     * @return the mean of all latencies for requests made
     */
    public float singleThreadLatencyMeasure(int numPosts) {

        // set a latch to wait until the thread is done making requests
        CountDownLatch gate = new CountDownLatch(1);
        Gates gates = new Gates(null, null, gate);

        // create and run thread skier ids and times are arbitrary
        Thread thread = new Thread(this.makeLiftPoster(1,100,91,330,numPosts, gates));
        thread.start();

        // wait for thread to finish
        try {
            gate.await();
        } catch (InterruptedException e) {
            // no need to catch as the count of requests is recorded
        }
        // remove the results from the master data list
        RequestData results = this.requestDataRepository.poll();

        // calculate the metrics, blocks until mean latency graph is manually closed
        results.getMetrics();

        // resets counters for actual tests
        super.resetAtomicCounters();

        // returns the avg latency
        return results.getMeanResponseTime();
    }

    /**
     * Creates the runnable for a ClientTwo thread
     * @param skierStart Skier id range start value
     * @param skierEnd Skier id range end value
     * @param startTime Time range start value
     * @param endTime Time range end value
     * @param numPosts Number of posts the thread needs to make
     * @param gates an object containing any relevant gates for the thread to wait on and/or countdown
     * @return the Runnable object
     */
    protected ClientTwoLiftPostingRunnable makeLiftPoster(int skierStart, int skierEnd, int startTime, int endTime,
        int numPosts, Gates gates) {
        return new ClientTwoLiftPostingRunnable
       (
           // skierID range
           skierStart, skierEnd,

           // start and end time in minutes
           startTime, endTime,

           // the number of posts to make and the max lift id
           numPosts, super.getNumLifts(),

           // the atomic integers for tracking successful and unsuccessful posts
           super.getSuccessfulRequests(), super.getUnsuccessfulRequests(),

           // Gate the thread increments on finish, gate the thread waits on, gate all threads increment on finish
           gates,

           // ipAddress and port to send the requests to
           super.ipAddress, super.port, super.client, this.requestDataRepository, super.runLocally
       );
    }

    /**
     * Runs all three phases of a ClientTwo test
     * @param args the relevant parameters for the test
     * @param filename the name for the csv file the results will be written to
     * @param measureLatency a boolean for whether or not to measure single-threaded latency before initiating the test
     * @return the total wall time taken for the test
     */
    private static long singleTestRun(String[] args, String filename, boolean measureLatency) {

        // start the filewriter, all threads will submit String records as they are recorded
        ThreadsafeFileWriter.setFilename(filename);
        ThreadsafeFileWriter.startInstance();

        // create client from provided args
        ClientTwo client = (ClientTwo) ArgParsingUtility.makeClient(args, 2);

        // measure single thread latency if needed
        if (measureLatency) {
            int latencyRequests = 1000;
            float latency = client.singleThreadLatencyMeasure(latencyRequests);
            System.out.printf("Average Latency in ms (single thread %d requests): %.1f\n", latencyRequests, latency);
        }

        // get the gate that only releases once all threads finish
        CountDownLatch finalGate = client.getFinalGate();

        // take starting timestamp
        LocalDateTime c1StartTime = LocalDateTime.now();

        // run all three phases, there are gates within the client that will control thread timing
        client.startPhaseOne();
        client.startPhaseTwo();
        client.startPhaseThree();

        // wait for threads to finish
        try {
            finalGate.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // take ending timestamp
        LocalDateTime c1EndTime = LocalDateTime.now();

        // calculate the C1 metrics for comparison
        client.calcC1Metrics(c1StartTime, c1EndTime, client);

        // Calculate and display the C2 metrics
        System.out.println("\nClientTwo-Style Metrics:");

        // set the head for a consolidated RequestData list
        RequestData head = client.requestDataRepository.poll();

        // merge all RequestData lists
        client.requestDataRepository.forEach(x->head.merge(x));

        // make the metrics, will block on displaying a line chart of mean latency over time (seconds)
        long wallTime = head.getMetrics();

        // close the threadPool
        client.threadPool.close();

        // close the fileWriter once it has finished writing (records are submitted from the runnable
        ThreadsafeFileWriter.finish();

        // return total wall time taken
        return wallTime;
    }

    /**
     * Helper to calculate and print the C1 style metrics
     * @param c1StartTime timestamp before phase one starts
     * @param c1EndTime timestamp before phase two starts
     * @param client ClientTwo instance
     */
    private void calcC1Metrics(LocalDateTime c1StartTime, LocalDateTime c1EndTime, ClientTwo client) {

        // get the duration of the run in seconds
        long duration = Duration.between(c1StartTime, c1EndTime).toSeconds();

        // calculate the average number of POST requests completed per second
        float requestsPerSecond = (float)
                                      (client.getSuccessfulTotal() + client.getUnsuccessfulTotal()) / duration;

        // print results
        System.out.println("ClientOne-Style Metrics:\n");
        System.out.println("Start time was: " + client.makeTime(c1StartTime) + " | End time was: " + client.makeTime(c1EndTime));
        System.out.println("Total time elapsed in seconds: " + duration);
        System.out.println("successful total: " + client.getSuccessfulTotal());
        System.out.println("unsuccessful total: " + client.getUnsuccessfulTotal());
        System.out.printf("requests per second: %.1f\n\n", requestsPerSecond);

    }


    public static void main(String[] args){
        // if not running the comparison for the assignment, do a single run
        if (! args[0].equals("assignmentMode")) {
            System.out.printf("time taken in milliseconds: %d", singleTestRun(args,"output",false));
        } else {

            boolean testLatency = true;

            // here we do the four runs for the assignment and put together the chart
            // define values for args for each run
            final String hardcodedIP = "35.172.135.219";
            final String SKIER_NUM = "20000";
            final String LIFT_NUM = "40";
            final String PORT_NUM = "8080";
            final int[] THREAD_NUMS = {32, 64, 128, 256};

            // Set up charting tool
            BarChartMaker bcm = new BarChartMaker("ClientTwo Thread Count Comparison", "NumThreads","Time Taken");

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
                bcm.addDatapoint(singleTestRun(arguments,threadNum + "Threads", testLatency),"CompletionTime",
                    String.valueOf(threadNum));

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
            } catch (InterruptedException | InvocationTargetException e) {
                e.printStackTrace();
            }

        }
    }
}
