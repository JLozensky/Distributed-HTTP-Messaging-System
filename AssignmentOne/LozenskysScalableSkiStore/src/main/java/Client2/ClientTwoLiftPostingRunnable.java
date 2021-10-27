package Client2;


import SharedClientClasses.AbstractLiftPosterRunnable;
import SharedClientClasses.Gates;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.httpclient.HttpClient;


public class ClientTwoLiftPostingRunnable extends AbstractLiftPosterRunnable {

    private RequestData requestData;
    private ConcurrentLinkedQueue<RequestData> requestDataRepository;


    public ClientTwoLiftPostingRunnable(int skierStart, int skierEnd, int startTime, int endTime, int numPosts,
        int numLifts, AtomicInteger successfulRequests, AtomicInteger unsuccessfulRequests, Gates gates,
        String ipAddress, String port, HttpClient client,
        ConcurrentLinkedQueue<RequestData> requestDataRepository, boolean runLocally) {

        super(skierStart, skierEnd, startTime, endTime, numPosts, numLifts, successfulRequests, unsuccessfulRequests,
            gates, client, ipAddress, port, runLocally);
        this.requestData = new RequestData();
        this.requestDataRepository = requestDataRepository;

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
        super.initializeInstanceVariables();
        super.getGates().waitToStart();

        for (int i = 0; i < super.getNumPosts(); i++) {
            super.prepNextRequest();
            boolean success = false;
            for (int numTries = 5; numTries > 0; numTries--) {
                try {
                    Instant start = Instant.now();
                    int statusCode = super.getClient().executeMethod(super.getPostMethod());
                    Instant end = Instant.now();
                    super.getPostMethod().getResponseBodyAsStream();
                    super.getPostMethod().releaseConnection();

                    ThreadsafeFileWriter.addRecord(this.requestData.addRecord(super.getPostMethod(),statusCode,start,end));

                    if (statusCode == 201) {
                        success = true;
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            super.incrementLocalResult(success);
        }

        this.requestDataRepository.add(this.requestData);

        super.finished();
    }

}
