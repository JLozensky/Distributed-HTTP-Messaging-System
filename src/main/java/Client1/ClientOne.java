package Client1;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientOne {



    private static final int SKI_DAY_MINUTES = 420;
    private static final float MAX_THREAD_Multiplier = 1.375f;
    private static final int PHASE_ONE_END = 90;
    private static final int PHASE_TWO_END = 360;

    private final int numThreads;
    private final int numSkiers;
    private final int numLifts;
    private final int meanLiftPerSkier;
    private final String ipAddress;
    private final String port;
    private final int quarterThreads;

    private int postNum;
    private int peakPostNum;
    private CountDownLatch phaseOneGate;
    private CountDownLatch phaseTwoGate;
    private CountDownLatch phaseThreeGate;
    private CountDownLatch finalGate;
    private ClientOneThreadPool threadPool;


    public ClientOne(int numThreads, int numSkiers, int numLifts, int meanLiftPerSkier,
        String ipAddress, String port){
        this.numThreads = numThreads;
        this.quarterThreads = numThreads / 4;
        this.numSkiers = numSkiers;
        this.numLifts = numLifts;
        this.meanLiftPerSkier = meanLiftPerSkier;
        this.ipAddress = ipAddress;
        this.port = port;
        this.setGates();
        this.setPostNums();
        this.threadPool = ClientOneThreadPool.getInstance(this.numThreads);

    }

    private void setPostNums() {
        this.postNum = (this.numLifts);
    }

    private void setGates() {
        this.phaseOneGate = new CountDownLatch((this.quarterThreads +9)/10);
        this.phaseTwoGate = new CountDownLatch((this.numThreads +9)/10);
        this.phaseThreeGate = new CountDownLatch(this.quarterThreads);
    }

    public boolean startPhaseOne() {



        return true;
    }

    @Override
    public String toString() {
        return "ClientOne{" +
                   "numThreads=" + numThreads +
                   ", numSkiers=" + numSkiers +
                   ", numLifts=" + numLifts +
                   ", meanLiftPerSkier=" + meanLiftPerSkier +
                   ", ipAddress='" + ipAddress + '\'' +
                   ", port='" + port + '\'' +
                   '}';
    }

    public static void main(String args[]){
        ClientOne client= ArgParsingUtility.makeClient(args);
        System.out.println(client.toString());
        boolean phaseOneComplete = client.startPhaseOne();


    }
}
