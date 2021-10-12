package Client1;

import SharedClientClasses.AbstractClient;
import SharedClientClasses.ArgParsingUtility;
import SharedClientClasses.Gates;
import com.github.sh0nk.matplotlib4j.Plot;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

public class ClientOne extends AbstractClient {


    public ClientOne(int numThreads, int numSkiers, int numLifts, int numRuns, String ipAddress, String port) {
        super(numThreads, numSkiers, numLifts,numRuns,ipAddress,port);
    }

        protected ClientOneLiftPostingRunnable makeLiftPoster(int skierStart, int skierEnd, int startTime, int endTime,
        int numPosts, Gates gates) {
        return new ClientOneLiftPostingRunnable
       (

           // skierID range
           skierStart, skierEnd,

           // start and end time in minutes
           startTime, endTime,

           // the number of posts to make and the max lift id
           numPosts, super.getNumLifts(),

           // the atomic integers for tracking successful and unsuccessful posts
           super.getSuccessfulRequests(), super.getUnsuccessfulRequests(),

           // Gates object with relevant CountDownLatches
           gates,

           // ipAddress and port to send the requests to
           super.client, super.ipAddress, super.port,  super.runLocally
       );
    }


    public static void main(String args[]){

        for (int i=0;i<1;i++) {
            // create client from provided args
            ClientOne client = (ClientOne) ArgParsingUtility.makeClient(args, 1);

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

            Plot plt = Plot.create();
            plt.hist();

        }
        System.exit(0);

    }




}
