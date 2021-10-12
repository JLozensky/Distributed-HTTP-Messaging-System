package SharedClientClasses;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

/**
 * Abstract client class holding most code shared between different Client implementations
 */
public abstract class AbstractClient {
    // constants provided by the assignment
    protected static final int MAX_TOTAL_CONNECTIONS = 256;
    protected static final int MAX_HOST_CONNECTIONS = 256;
    private static final int SKI_DAY_MINUTES = 420;
    private static final int PHASE_ONE_END = 90;
    private static final int PHASE_TWO_END = 360;

    // instance variables for each test run
    protected final int totalThreads;
    protected final int peakThreads;
    protected final int numLifts;
    protected final String ipAddress;
    protected final String port;
    protected final int nonPeakThreads;
    protected final int peakSkiersPerThread;
    protected final int nonPeakSkiersPerThread;
    protected final int tenPercentPeakThreads;
    protected final int tenPercentNonPeakThreads;
    protected int peakPostNum;
    protected int nonPeakPostNum;
    protected HttpClient client;
    protected ClientThreadPool threadPool;
    protected boolean runLocally;
    private CountDownLatch phaseOneGate;
    private CountDownLatch phaseTwoGate;
    private CountDownLatch finalGate;
    private AtomicInteger successfulRequests = new AtomicInteger();
    private AtomicInteger unsuccessfulRequests = new AtomicInteger();

    /**
     * Constructor that takes in the parameters parsed from command-line entry and converts them to the calculated
     * values used in running the simulated post request test
     *
     * Values are calculated here to make running the threads more expedient
     *
     * @param numThreads The base number of threads to use for calculations as described in the assignment requirements
     * @param numSkiers The base number of skiers to use for calculations as described in the assignment requirements
     * @param numLifts The range of lift IDs to randomly generate from for each post request
     * @param numRuns The average number of runs per skier to use for calculations as described in the assignment
     *                requirements
     * @param ipAddress the ipAddress the server is running on
     * @param port the port the server is listening on
     */
    public AbstractClient(int numThreads, int numSkiers, int numLifts, int numRuns,
        String ipAddress, String port) {

        // all calculations are split into peak and non-peak equivalents
        // calculating the number of threads to be used for respective phases
        this.peakThreads = numThreads;
        this.nonPeakThreads = numThreads / 4;
        this.totalThreads = this.peakThreads + this.nonPeakThreads * 2;
        this.tenPercentPeakThreads = (this.peakThreads + 9) / 10;
        this.tenPercentNonPeakThreads = (this.nonPeakThreads + 9) / 10;

        // calculate the size of ranges of skier Ids to be assigned to each thread
        this.peakSkiersPerThread = numSkiers / this.peakThreads;
        this.nonPeakSkiersPerThread = numSkiers / this.nonPeakThreads;

        // calculate the number of POST requests each thread will need to send
        this.peakPostNum = (int) Math.ceil((numRuns * .6) * this.peakSkiersPerThread);
        this.nonPeakPostNum = (int) Math.ceil((numRuns * .2) * this.nonPeakSkiersPerThread);

        // assign values that don't need calculations
        this.numLifts = numLifts;
        this.ipAddress = ipAddress;
        this.port = port;

        // Initialize and set instance variables used in making POST requests
        this.setThreadPool();
        this.setRunLocally();
        this.setGates();
        this.createHttpClient();
    }

    /**
     * Get a reference to the singleton instance of the threadpool to be used for the duration of the client's test(s)
     * Sets the max fixed threads to the total possible number of concurrent threads (all threads minus the 10%
     * required to move past phase one and the 10% for phase two)
     */
    protected void setThreadPool() {
        this.threadPool = ClientThreadPool.getInstance(
            this.totalThreads - this.tenPercentNonPeakThreads - this.tenPercentPeakThreads
        );
    }

    /**
     * This sets a boolean of whether the test is targeting a server run locally vs on the ec2 instance.
     * The LiftPostingRunnable utilizes this to properly format the POST request Strings
     */
    protected void setRunLocally(){

        if (this.ipAddress.equals("localhost")) {
            this.runLocally = true;
        } else {
            this.runLocally = false;
        }
    }

    /**
     * Creates an httpclient with a multithreaded connection manager so all threads can use the same client
     */
    protected void createHttpClient() {
        this.client = new HttpClient(new MultiThreadedHttpConnectionManager());
        HttpConnectionManagerParams params = new HttpConnectionManagerParams();
        params.setDefaultMaxConnectionsPerHost(MAX_HOST_CONNECTIONS);
        params.setMaxTotalConnections(MAX_TOTAL_CONNECTIONS);

        this.client.getHttpConnectionManager().setParams(params);
    }

    /**
     * Provides values of most instance variables when using toString
     * @return String of instance variables
     */
    @Override
    public String toString() {
        return "ClientOne{" +
                   "totalThreads=" + totalThreads +
                   ", peakThreads=" + peakThreads +
                   ", numLifts=" + numLifts +
                   ", ipAddress='" + ipAddress + '\'' +
                   ", port='" + port + '\'' +
                   ", nonPeakThreads=" + nonPeakThreads +
                   ", peakSkiersPerThread=" + peakSkiersPerThread +
                   ", nonPeakSkiersPerThread=" + nonPeakSkiersPerThread +
                   ", tenPercentPeakThreads=" + tenPercentPeakThreads +
                   ", tenPercentNonPeakThreads=" + tenPercentNonPeakThreads +
                   ", peakPostNum=" + peakPostNum +
                   ", nonPeakPostNum=" + nonPeakPostNum +
                   ", phaseOneGate=" + phaseOneGate +
                   ", phaseTwoGate=" + phaseTwoGate +
                   ", finalGate=" + finalGate +
                   ", client=" + client +
                   ", threadPool=" + threadPool +
                   ", successfulRequests=" + successfulRequests +
                   ", unsuccessfulRequests=" + unsuccessfulRequests +
                   "}\n\n";
    }

    /**
     * Sets the gates used for timing the various phases and timestamps
     */
    protected void setGates() {
        // 10% of phase one threads need to countdown this latch before phase two threads can run
        this.phaseOneGate = new CountDownLatch(this.tenPercentNonPeakThreads);

        // 10% of phase two threads need to countdown this latch before phase three threads can run
        this.phaseTwoGate = new CountDownLatch(this.tenPercentPeakThreads);

        // The total number of threads used throughout all phases need to coundown this latch before the final timestamp
        // can be taken and the program allowed to end/move on
        this.finalGate = new CountDownLatch(this.totalThreads);
    }

    /**
     * Abstract method requiring the individual clients to configure the creation of their respective
     * LiftPosterRunnable objects
     * @param skierStart Skier id range start value
     * @param skierEnd Skier id range end value
     * @param startTime Time range start value
     * @param endTime Time range end value
     * @param numPosts Number of posts the thread needs to make
     * @param gates an object containing any relevant gates for the thread to wait on and/or countdown
     * @return The runnable to be given to the central thread pool ExecutorService
     */
    protected abstract AbstractLiftPosterRunnable makeLiftPoster(int skierStart, int skierEnd, int startTime,
        int endTime, int numPosts, Gates gates);

    /**
     * getter for the gate that only releases when all threads are done
     * @return the final gate for the Client's instance based on the instantiation parameters
     */
    public CountDownLatch getFinalGate() {
        return this.finalGate;
    }

    /**
     * Getter for the value of the AtomicInteger tracking successful requests
     * @return number of successful requests
     */
    public int getSuccessfulTotal() {
        return this.successfulRequests.get();
    }

    /**
     * Getter for the value of the AtomicInteger tracking unsuccessful requests
     * @return number of unsuccessful requests
     */
    public int getUnsuccessfulTotal() {
        return this.unsuccessfulRequests.get();
    }

    /**
     * Drives creation of runnable tasks for phase one and gives them to the Executor service to find a thread
     */
    public void startPhaseOne() {
//        System.out.println(this.toString());
        int skierStart = 0;
        int skierEnd = 0;

        for (int i = 0; i < this.nonPeakThreads; i++) {
            skierStart = skierEnd + 1;
            skierEnd = skierEnd + this.nonPeakSkiersPerThread;

            this.threadPool.run(
                this.makeLiftPoster(
                    skierStart, skierEnd, 1, PHASE_ONE_END, this.nonPeakPostNum,
                    new Gates(null, this.phaseOneGate, this.finalGate)
                )
            );
        }
    }

    /**
     * Drives creation of runnable tasks for phase two and gives them to the Executor service to find a thread
     */
    public void startPhaseTwo() {
        int skierStart = 0;
        int skierEnd = 0;
        int startTime = PHASE_ONE_END + 1;

        for (int i = 0; i < this.peakThreads; i++) {
            skierStart = skierEnd + 1;
            skierEnd = skierEnd + this.peakSkiersPerThread;

            this.threadPool.run(
                this.makeLiftPoster(
                    skierStart, skierEnd, startTime, PHASE_TWO_END, this.peakPostNum,
                    new Gates(this.phaseOneGate, this.phaseTwoGate, this.finalGate)
                )
            );
        }
    }

    /**
     * Drives creation of runnable tasks for phase three and gives them to the Executor service to find a thread
     */
    public void startPhaseThree() {
        int skierStart = 0;
        int skierEnd = 0;
        int startTime = PHASE_TWO_END + 1;

        for (int i = 0; i < this.nonPeakThreads; i++) {
            skierStart = skierEnd + 1;
            skierEnd = skierEnd + this.nonPeakSkiersPerThread;

            this.threadPool.run(
                this.makeLiftPoster(
                    skierStart, skierEnd, startTime, SKI_DAY_MINUTES, this.nonPeakPostNum,
                    new Gates(this.phaseTwoGate, null, this.finalGate)
                )
            );
        }
    }

    /**
     * utility method to make an hour:minute:second time representation string
     * @param time the LocalDateTime object to parse
     * @return formatted String
     */
    public String makeTime(LocalDateTime time) {
        return String.format("%d:%d:%d", time.getHour(), time.getMinute(), time.getSecond());
    }

    /**
     *  getter for number of lifts at the fictional resort this client creates records for
     * @return max lift id number
     */
    protected int getNumLifts() {
        return this.numLifts;
    }

    /**
     * Getter for concurrent tracker of successful requests
     * @return The reference to the AtomicInteger tracking successful requests
     */
    protected AtomicInteger getSuccessfulRequests() {
        return this.successfulRequests;
    }

    /**
     * Getter for concurrent tracker of unsuccessful requests
     * @return The reference to the AtomicInteger tracking unsuccessful requests
     */
    protected AtomicInteger getUnsuccessfulRequests() {
        return this.unsuccessfulRequests;
    }

    /**
     * resets request success and non-success trackers to 0
     */
    protected void resetAtomicCounters() {
        this.successfulRequests.set(0);
        this.unsuccessfulRequests.set(0);
    }
}
