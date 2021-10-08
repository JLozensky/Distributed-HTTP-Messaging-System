package Client1;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

public class ClientOne {



    private static final int SKI_DAY_MINUTES = 420;
    private static final int PHASE_ONE_END = 90;
    private static final int PHASE_TWO_END = 360;
    private static final int MAX_TOTAL_CONNECTIONS= 20;
    private static final int MAX_HOST_CONNECTIONS= 20;

    private final int totalThreads;
    private final int peakThreads;
    private final int numLifts;
    private final String ipAddress;
    private final String port;
    private final int nonPeakThreads;
    private final int peakSkiersPerThread;
    private final int nonPeakSkiersPerThread;
    private final int tenPercentPeakThreads;
    private final int tenPercentNonPeakThreads;

    private int peakPostNum;
    private int nonPeakPostNum;
    private CountDownLatch phaseOneGate;
    private CountDownLatch phaseTwoGate;
    private CountDownLatch finalGate;
    private HttpClient client;
    private ClientOneThreadPool threadPool;
    private AtomicInteger successfulRequests = new AtomicInteger();
    private AtomicInteger unsuccessfulRequests = new AtomicInteger();


    public ClientOne(int numThreads, int numSkiers, int numLifts, int numRuns,
        String ipAddress, String port){

        this.peakThreads = numThreads;
        this.nonPeakThreads = numThreads / 4;
        this.totalThreads = this.peakThreads + this.nonPeakThreads * 2;
        this.tenPercentNonPeakThreads = (this.nonPeakThreads +9) / 10;
        this.tenPercentPeakThreads = (this.peakThreads+9) /10;

        this.peakSkiersPerThread = numSkiers / this.peakThreads;
        this.nonPeakSkiersPerThread = numSkiers / this.nonPeakThreads;

        this.peakPostNum = (int) Math.ceil((numRuns * .6) * this.peakSkiersPerThread);
        this.nonPeakPostNum = (int) Math.ceil((numRuns * .2) * this.nonPeakSkiersPerThread);

        this.numLifts = numLifts;
        this.ipAddress = ipAddress;
        this.port = port;
        this.setGates();

        // Create thread pool with max possible concurrent threads
        this.threadPool = ClientOneThreadPool.getInstance(
            this.totalThreads - this.tenPercentNonPeakThreads - this.tenPercentPeakThreads
        );

        this.client = new HttpClient(new MultiThreadedHttpConnectionManager());
        HttpConnectionManagerParams params = new HttpConnectionManagerParams();
        params.setDefaultMaxConnectionsPerHost(MAX_HOST_CONNECTIONS);
        params.setMaxTotalConnections(MAX_TOTAL_CONNECTIONS);
        this.client.getHttpConnectionManager().setParams(params);

    }

    private void setGates() {
        this.phaseOneGate = new CountDownLatch(this.tenPercentNonPeakThreads);
        this.phaseTwoGate = new CountDownLatch(this.tenPercentPeakThreads);
        this.finalGate = new CountDownLatch(this.totalThreads);
    }

    private LiftPostingRunnable makeLiftPoster(int skierStart, int skierEnd, int startTime, int endTime, int numPosts,
        CountDownLatch countGate, CountDownLatch waitGate) {
        return new LiftPostingRunnable
       (
            // skierID range
            skierStart, skierEnd,

            // start and end time in minutes
            startTime, endTime,

            // the number of posts to make and the max lift id
            numPosts, this.numLifts,

            // the atomic integers for tracking successful and unsuccessful posts
           this.successfulRequests, this.unsuccessfulRequests,

            // Gate the thread increments on finish, gate the thread waits on, gate all threads increment on finish
            countGate,waitGate, this.finalGate,

           // ipAddress and port to send the requests to
           this.ipAddress, this.port, this.client
       );
    }

    public CountDownLatch getFinalGate() {
        return this.finalGate;
    }

    private int getSuccessfulTotal() {
        return this.successfulRequests.get();
    }

    private int getUnsuccessfulTotal(){
        return this.unsuccessfulRequests.get();
    }

    public void startPhaseOne() {
        int skierStart = 0;
        int skierEnd = 0;

        for (int i = 0; i < this.nonPeakThreads; i++){
            skierStart = skierEnd + 1;
            skierEnd = skierEnd + this.nonPeakSkiersPerThread;

            this.threadPool.run(
                this.makeLiftPoster(
                    skierStart, skierEnd, 1, PHASE_ONE_END, this.nonPeakPostNum,this.phaseOneGate,null
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
                    skierStart, skierEnd, startTime, PHASE_TWO_END, this.peakPostNum, this.phaseTwoGate,
                    this.phaseOneGate
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
                    skierStart, skierEnd, startTime, SKI_DAY_MINUTES, this.nonPeakPostNum, null, this.phaseTwoGate
                )
            );
        }
    }

    private void makeThreads(int skierGap, int startTime, int endTime, int numPosts, CountDownLatch countGate,
        CountDownLatch waitGate) {

    }


    public static void main(String args[]){
        // create client from provided args
        ClientOne client= ArgParsingUtility.makeClient(args);

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

        System.out.println(endTime.compareTo(startTime));
        System.out.println("successful total" + client.getSuccessfulTotal());
        System.out.println("unsuccessful total" + client.getUnsuccessfulTotal());

    }




}
