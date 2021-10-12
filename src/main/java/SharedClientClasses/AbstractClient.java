package SharedClientClasses;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

public abstract class AbstractClient {

    protected static final int MAX_TOTAL_CONNECTIONS = 20;
    protected static final int MAX_HOST_CONNECTIONS = 20;
    private static final int SKI_DAY_MINUTES = 420;
    private static final int PHASE_ONE_END = 90;
    private static final int PHASE_TWO_END = 360;
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

    public AbstractClient(int numThreads, int numSkiers, int numLifts, int numRuns,
        String ipAddress, String port) {

        this.peakThreads = numThreads;
        this.nonPeakThreads = numThreads / 4;
        this.totalThreads = this.peakThreads + this.nonPeakThreads * 2;
        this.tenPercentPeakThreads = (this.peakThreads + 9) / 10;
        this.tenPercentNonPeakThreads = (this.nonPeakThreads + 9) / 10;

        this.peakSkiersPerThread = numSkiers / this.peakThreads;
        this.nonPeakSkiersPerThread = numSkiers / this.nonPeakThreads;

        this.peakPostNum = (int) Math.ceil((numRuns * .6) * this.peakSkiersPerThread);
        this.nonPeakPostNum = (int) Math.ceil((numRuns * .2) * this.nonPeakSkiersPerThread);

        this.numLifts = numLifts;
        this.ipAddress = ipAddress;
        this.port = port;

        this.setThreadPool();
        this.setRunLocally();
        this.setGates();
        this.createHttpClient();
    }

    protected void setThreadPool() {
        this.threadPool = ClientThreadPool.getInstance(
            this.totalThreads - this.tenPercentNonPeakThreads - this.tenPercentPeakThreads
        );
    }

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
                   '}';
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
        System.out.println(this.toString());
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
        return String.format("Start time was: %d:%d:%d", time.getHour(), time.getMinute(), time.getSecond());
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
}
