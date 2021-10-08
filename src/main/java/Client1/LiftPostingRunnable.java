package Client1;


import SharedLibrary.LiftRide;
import SharedLibrary.StatusCodes;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;


import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class LiftPostingRunnable implements Runnable{
    private int skierStart;
    private int skierEnd;
    private int startTime;
    private int endTime;
    private int numPosts;
    private int numLifts;
    private CountDownLatch countGate;
    private CountDownLatch waitGate;
    private CountDownLatch finalGate;
    private HttpClient client;
    private String ipAddress;
    private String port;
    private PostMethod postMethod;
    private ThreadLocalRandom randomGen;
    private StringBuilder uriStringBuilder;
    private int curLift;
    private int curTime;
    private static final int RESORT_ID = 1;
    private static final int SEASON_ID = 2021;
    private static final int DAY_ID = 42;
    private static final String localHost = "LozenskysScalableSkiStore_war_exploded";
    private static final String ec2Host = "LozenskysScalableSkiStore_war";

    private Gson gson;
    private AtomicInteger globalSuccessCount;
    private AtomicInteger globalFailureCount;
    private int localSuccessCount;
    private int localFailureCount;
    private int curSkierID;
    private int uriBuilderLength;

    public LiftPostingRunnable(int skierStart, int skierEnd, int startTime, int endTime, int numPosts,
        int numLifts, AtomicInteger successfulRequests, AtomicInteger unsuccessfulRequests, CountDownLatch countGate,
        CountDownLatch waitGate, CountDownLatch finalGate, String ipAddress, String port, HttpClient client) {

        this.skierStart = skierStart;
        this.skierEnd = skierEnd;

        this.startTime = startTime;
        this.endTime = endTime;

        this.numLifts = numLifts;

        this.numPosts = numPosts;

        this.countGate = countGate;
        this.waitGate = waitGate;
        this.finalGate = finalGate;
        this.ipAddress = ipAddress;
        this.port = port;
        this.client = client;
        this.globalSuccessCount = successfulRequests;
        this.globalFailureCount = unsuccessfulRequests;

    }

    /**
     * When an object implementing interface <code>Runnable</code> is used to create a thread, starting the thread
     * causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        this.initializeInstanceVariables();

        if (this.waitGate != null) {
            try {
                this.waitGate.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < this.numPosts; i++) {
            this.prepNextRequest();
            boolean success = false;
            for (int numTries = 5; numTries > 0; numTries--) {
                try {
                    int statusCode = this.client.executeMethod(this.postMethod);
                    this.postMethod.releaseConnection();
                    if (statusCode == 201) {
                        success = true;
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (success) {
                localSuccessCount += 1;
            } else {
                localFailureCount += 1;
            }
        }
        if (this.localSuccessCount > 0) {
            this.globalSuccessCount.addAndGet(this.localSuccessCount);
        }
        if (this.localFailureCount > 0) {
            this.globalFailureCount.addAndGet(this.localFailureCount);
        }
        if (this.countGate != null) {
            this.countGate.countDown();
        }
        this.finalGate.countDown();
    }

    private void initializeInstanceVariables() {
        this.postMethod = new PostMethod();
        this.postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
            new DefaultHttpMethodRetryHandler(5,false));
        this.randomGen = ThreadLocalRandom.current();
        this.localSuccessCount = 0;
        this.localFailureCount = 0;
        this.initializeURIBuilder();
        this.gson = new Gson();
    }

    private void prepNextRequest() {
        this.curSkierID = this.numberGenerator(this.skierStart,this.skierEnd);
        this.curLift = this.numberGenerator(1,this.numLifts);
        this.curTime = this.numberGenerator(startTime,endTime);
        this.setURI();
        System.out.println("skier: " + this.curSkierID +" -  lift: " + this.curLift + " -  time: " + this.curTime);
        try {
            StringRequestEntity entity = new StringRequestEntity(
                gson.toJson(
                    new LiftRide(this.curTime,this.curLift)
                ),
                "application/java",
                "UTF-8"
            );
            this.postMethod.setRequestEntity(entity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    private void initializeURIBuilder(){
        this.uriStringBuilder = new StringBuilder().append("http://").append(this.ipAddress).append(":")
            .append(this.port).append("/").append(localHost).append("/skiers/").append(RESORT_ID).append("/seasons/")
            .append(SEASON_ID).append("/days/").append(DAY_ID).append("/skiers/");
        this.uriBuilderLength = this.uriStringBuilder.length();
    }

    private void setURI(){
        this.uriStringBuilder.append(this.curSkierID);

        try {
            this.postMethod.setURI(new URI(this.uriStringBuilder.toString(),false));
        } catch (URIException e) {
            e.printStackTrace();
        }

        this.uriStringBuilder.setLength(this.uriBuilderLength);
    }
    private int numberGenerator(int start, int end) {
        return this.randomGen.nextInt(start,end+1);
    }

}
