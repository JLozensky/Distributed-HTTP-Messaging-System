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
    protected static final int MAX_TOTAL_CONNECTIONS = 20;
    protected static final int MAX_HOST_CONNECTIONS = 20;
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

    protected void createHttpClient() {
        this.client = new HttpClient(new MultiThreadedHttpConnectionManager());
        HttpConnectionManagerParams params = new HttpConnectionManagerParams();
        params.setDefaultMaxConnectionsPerHost(MAX_HOST_CONNECTIONS);
        params.setMaxTotalConnections(MAX_TOTAL_CONNECTIONS);

        this.client.getHttpConnectionManager().setParams(params);
    }


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

    protected void setGates() {
        this.phaseOneGate = new CountDownLatch(this.tenPercentNonPeakThreads);
        this.phaseTwoGate = new CountDownLatch(this.tenPercentPeakThreads);
        this.finalGate = new CountDownLatch(this.totalThreads);
    }

    protected abstract AbstractLiftPosterRunnable makeLiftPoster(int skierStart, int skierEnd, int startTime,
        int endTime, int numPosts, Gates gates);

    public CountDownLatch getFinalGate() {
        return this.finalGate;
    }

    public ClientThreadPool getThreadPool() {
        return this.threadPool;
    }

    public int getSuccessfulTotal() {
        return this.successfulRequests.get();
    }

    public int getUnsuccessfulTotal() {
        return this.unsuccessfulRequests.get();
    }

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



    public String makeTime(LocalDateTime time) {
        return String.format("%d:%d:%d", time.getHour(), time.getMinute(), time.getSecond());
    }

    protected int getNumLifts() {
        return this.numLifts;
    }

    protected AtomicInteger getSuccessfulRequests() {
        return this.successfulRequests;
    }

    protected AtomicInteger getUnsuccessfulRequests() {
        return this.unsuccessfulRequests;
    }

    protected void resetAtomicCounters() {
        this.successfulRequests.set(0);
        this.unsuccessfulRequests.set(0);
    }
}
