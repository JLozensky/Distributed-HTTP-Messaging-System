package Client1;


import SharedLibrary.LiftRide;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;



import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class LiftPostingRunnable implements Runnable{
    private final int skierStart;
    private final int skierEnd;
    private final int startTime;
    private final int endTime;
    private final int numPosts;
    private final int numLifts;
    private final CountDownLatch countGate;
    private final CountDownLatch waitGate;
    private final CountDownLatch finalGate;
    private final HttpClient client;
    private final String ipAddress;
    private final String port;
    private PostMethod postMethod;
    private ThreadLocalRandom randomGen;
    private StringBuilder uriStringBuilder;
    private static final int RESORT_ID = 1;
    private static final int SEASON_ID = 2021;
    private static final int DAY_ID = 42;
    private static final String localHost = "LozenskysScalableSkiStore_war_exploded";
    private static final String ec2Host = "LozenskysScalableSkiStore_war";

    private Gson gson;
    private final AtomicInteger globalSuccessCount;
    private final AtomicInteger globalFailureCount;
    private int localSuccessCount;
    private int localFailureCount;
    private int curSkierID;
    private int uriBuilderLength;

    private final int threadNum;

    public LiftPostingRunnable(int skierStart, int skierEnd, int startTime, int endTime, int numPosts,
        int numLifts, AtomicInteger successfulRequests, AtomicInteger unsuccessfulRequests, CountDownLatch countGate,
        CountDownLatch waitGate, CountDownLatch finalGate, String ipAddress, String port, HttpClient client, int threadNum) {

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
        this.threadNum = threadNum;
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
//        System.out.println(this.threadNum + " is starting");
        while (this.localFailureCount + this.localSuccessCount < this.numPosts) {
            this.prepNextRequest();
            boolean success = false;
            for (int numTries = 5; numTries > 0; numTries--) {
                try {

                    int statusCode = this.client.executeMethod(this.postMethod);
                    this.postMethod.getResponseBodyAsStream();
                    this.postMethod.releaseConnection();

                    if (statusCode == 201) {
//                        this.postMethod.releaseConnection();
                        success = true;
                        break;
                    }
//                    this.postMethod.releaseConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                finally {
//                    this.postMethod.releaseConnection();
//                }
            }

            if (success) {
//                System.out.println("yay thread " + this.threadNum + " post " + (this.localSuccessCount + this.localFailureCount));
                localSuccessCount += 1;
            } else {
//                System.out.println("boo thread " + this.threadNum + " post " + (this.localSuccessCount + this.localFailureCount));
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
//        System.out.println(this.threadNum + " is ending");

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
        int curLift = this.numberGenerator(1, this.numLifts);
        int curTime = this.numberGenerator(startTime, endTime);
        this.postMethod = new PostMethod(getURL());
        try {
            StringRequestEntity entity = new StringRequestEntity(
                gson.toJson(
                    new LiftRide(curTime, curLift)
                ),
                "application/json",
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

    private String getURL(){
        this.uriStringBuilder.append(this.curSkierID);
        String temp = this.uriStringBuilder.toString();
        this.uriStringBuilder.setLength(this.uriBuilderLength);
        return temp;
    }


    private int numberGenerator(int start, int end) {
        return this.randomGen.nextInt(start,end+1);
    }

}
