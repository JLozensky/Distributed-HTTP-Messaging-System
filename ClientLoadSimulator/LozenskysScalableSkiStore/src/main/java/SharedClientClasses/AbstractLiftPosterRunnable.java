package SharedClientClasses;

import DataObjects.LiftRide;
import com.google.gson.Gson;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;

public abstract class AbstractLiftPosterRunnable implements Runnable {

    private final int RESORT_ID = 1;
    private final int SEASON_ID = 2021;
    private final int DAY_ID = 42;
    private final String LOCAL_HOST = "LozenskysScalableSkiStore_war_exploded";
    private final String EC2_HOST = "LozenskysScalableSkiStore_war";
    protected final int skierStart;
    protected final int skierEnd;
    protected final int startTime;
    protected final int endTime;
    protected final int numPosts;
    protected final int numLifts;
    protected final Gates gates;
    protected HttpClient client;
    protected final String ipAddress;
    protected final String port;
    protected final AtomicInteger globalSuccessCount;
    protected final AtomicInteger globalFailureCount;
    protected boolean runLocally;
    private PostMethod postMethod;
    private ThreadLocalRandom randomGen;
    private StringBuilder uriStringBuilder;
    private Gson gson;
    private int localSuccessCount;
    private int localFailureCount;
    private int curSkierID;
    private int uriBuilderLength;

    public AbstractLiftPosterRunnable(
        int skierStart, int skierEnd, int startTime, int endTime, int numPosts, int numLifts,
        AtomicInteger successfulRequests, AtomicInteger unsuccessfulRequests, Gates gates, HttpClient client,
        String ipAddress, String port,
        boolean runLocally) {

        this.skierStart = skierStart;
        this.skierEnd = skierEnd;
        this.startTime = startTime;
        this.endTime = endTime;
        this.numPosts = numPosts;
        this.numLifts = numLifts;
        this.gates = gates;
        this.client = client;
        this.ipAddress = ipAddress;
        this.port = port;
        this.globalSuccessCount = successfulRequests;
        this.globalFailureCount = unsuccessfulRequests;
        this.runLocally = runLocally;
    }

    protected void incrementLocalResult(boolean success){
        if (success) {
            this.localSuccessCount += 1;
        } else {
            this.localFailureCount += 1;
        }
    }

    protected void initializeInstanceVariables() {
        this.postMethod = new PostMethod();
        this.postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
            new DefaultHttpMethodRetryHandler(5, false));
        this.randomGen = ThreadLocalRandom.current();
        this.localSuccessCount = 0;
        this.localFailureCount = 0;
        this.initializeURIBuilder();
        this.gson = new Gson();
    }

    protected void prepNextRequest() {
        this.curSkierID = this.numberGenerator(this.skierStart, this.skierEnd);
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

    private void initializeURIBuilder() {
        String fileString = null;

        if (this.runLocally) {
            fileString = LOCAL_HOST;
        } else {
            fileString = EC2_HOST;
        }

        this.uriStringBuilder = new StringBuilder().append("http://").append(this.ipAddress).append(":")
            .append(this.port).append("/").append(fileString).append("/skiers/").append(RESORT_ID).append("/seasons/")
            .append(SEASON_ID).append("/days/").append(DAY_ID).append("/skiers/");
        this.uriBuilderLength = this.uriStringBuilder.length();
    }

    private String getURL() {
        this.uriStringBuilder.append(this.curSkierID);
        String temp = this.uriStringBuilder.toString();
        this.uriStringBuilder.setLength(this.uriBuilderLength);
        return temp;
    }

    protected void finished(){
        if (this.localSuccessCount > 0) {
            this.globalSuccessCount.addAndGet(this.localSuccessCount);
        }
        if (this.localFailureCount > 0) {
            this.globalFailureCount.addAndGet(this.localFailureCount);
        }
        this.gates.finishedCountdown();

    }

    public int getNumPosts() {
        return this.numPosts;
    }

    public Gates getGates() {
        return this.gates;
    }

    public PostMethod getPostMethod() {
        return postMethod;
    }

    private int numberGenerator(int start, int end) {
        return this.randomGen.nextInt(start, end + 1);
    }

    protected HttpClient getClient(){
        return this.client;
    }

}
