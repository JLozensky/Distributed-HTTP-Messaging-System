package Client2;

import SharedClientClasses.AbstractClient;
import SharedClientClasses.ArgParsingUtility;
import SharedClientClasses.Gates;
import SharedClientClasses.RequestData;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientTwo extends AbstractClient {

    private AtomicInteger successfulRequests = new AtomicInteger();
    private AtomicInteger unsuccessfulRequests = new AtomicInteger();
    private ConcurrentLinkedQueue<RequestData> requestDataRepository;


    public ClientTwo(int numThreads, int numSkiers, int numLifts, int numRuns, String ipAddress, String port){

        super(numThreads, numSkiers, numLifts,numRuns,ipAddress,port);

        this.requestDataRepository = new ConcurrentLinkedQueue<>();
    }


    public RequestData singleThreadLatencyMeasure(int numPosts) {
        RequestData results = new RequestData();
        CountDownLatch gate = new CountDownLatch(numPosts);
        Gates gates = new Gates(null, null, gate);
        this.makeLiftPoster(1,50,1,50,numPosts, gates);
        try {
            gate.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return results;
    }

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

    private ConcurrentLinkedQueue<RequestData> getRequestDataRepository() { return this.requestDataRepository; }

    public static void main(String args[]){

        for (int i=0;i<1;i++) {
            // create client from provided args
            ClientTwo client = (ClientTwo) ArgParsingUtility.makeClient(args, 2);

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

            long duration = Duration.between(startTime, endTime).toMillis();

            float requestsPerSecond =
                (client.getSuccessfulTotal() + client.getUnsuccessfulTotal()) / (duration / 1000f);
            System.out.println("Start time was: " + client.makeTime(startTime));
            System.out.println("End time was: " + client.makeTime(endTime));
            System.out.println("successful total: " + client.getSuccessfulTotal());
            System.out.println("unsuccessful total: " + client.getUnsuccessfulTotal());
            System.out.println("time taken in milliseconds: " + duration);
            System.out.printf("requests per second: %.1f", requestsPerSecond);
        }
        System.exit(0);

    }




}
