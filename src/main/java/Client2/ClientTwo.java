package Client2;

import SharedClientClasses.AbstractClient;
import SharedClientClasses.ArgParsingUtility;
import SharedClientClasses.BarChartMaker;
import SharedClientClasses.Gates;
import SharedClientClasses.RequestData;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class ClientTwo extends AbstractClient {


    private ConcurrentLinkedQueue<RequestData> requestDataRepository;


    public ClientTwo(int numThreads, int numSkiers, int numLifts, int numRuns, String ipAddress, String port){

        super(numThreads, numSkiers, numLifts, numRuns, ipAddress, port);

        this.requestDataRepository = new ConcurrentLinkedQueue<>();
    }


    public float singleThreadLatencyMeasure(int numPosts) {
        RequestData results = new RequestData();
        CountDownLatch gate = new CountDownLatch(numPosts);
        Gates gates = new Gates(null, null, gate);
        this.makeLiftPoster(1,50,1,50,numPosts, gates);
        try {
            gate.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        results.getMetrics();

        return results.getMeanResponseTime();
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

    private static long singleTestRun(String args[], String filename, boolean measureLatency) {
        ThreadsafeFileWriter.setFilename(filename);
        ThreadsafeFileWriter.startInstance();



        // create client from provided args
        ClientTwo client = (ClientTwo) ArgParsingUtility.makeClient(args, 2);

        if (measureLatency) {
            client.singleThreadLatencyMeasure(1000);
        }

        // get the gate that only releases once all threads finish
        CountDownLatch finalGate = client.getFinalGate();


        // run all three phases, there are gates within the client that will control thread timing
        client.startPhaseOne();
        client.startPhaseTwo();
        client.startPhaseThree();

        try {
            finalGate.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        RequestData head = client.requestDataRepository.poll();
        client.requestDataRepository.forEach(x->head.merge(x));
        long wallTime = head.getMetrics();

        System.out.println("successful total: " + client.getSuccessfulTotal());
        System.out.println("unsuccessful total: " + client.getUnsuccessfulTotal());

        ThreadsafeFileWriter.finish();

        return wallTime;
    }


    public static void main(String args[]){
        // if not running the comparison for the assignment, do a single run
        if (! args[0].equals("assignmentMode")) {
            System.out.printf("time taken in milliseconds: %d", singleTestRun(args,"output",false));
        } else {

            boolean testLatency = true;

            // here we do the four runs for the assignment and put together the chart
            // define values for args for each run
            final String hardcodedIP = "3.91.230.49";
            final String SKIER_NUM = "20000";
            final String LIFT_NUM = "40";
            final String PORT_NUM = "8080";
            final int[] THREAD_NUMS = {32,64,128,256};

            // Set up charting tool
            BarChartMaker bcm = new BarChartMaker("ClientOne Thread Count Comparison", "NumThreads","Time Taken");

            // for each thread num to test
            for (int threadNum:THREAD_NUMS) {

                // create an args string as if the commands were passed in via commandline
                String[] arguments = {
                    "-t", String.valueOf(threadNum),
                    "-s", SKIER_NUM,
                    "-l", LIFT_NUM,
                    "-p", PORT_NUM,
                    "-ip", hardcodedIP
                };


                // add the datapoint of time taken to the graph
                bcm.addDatapoint(singleTestRun(arguments,threadNum + "Threads", testLatency),"CompletionTime",
                    String.valueOf(threadNum));

                // set to false so we only test latency once
                testLatency = false;
            }

            try {
                SwingUtilities.invokeAndWait(()-> {
                    bcm.makeChart();
                    bcm.setSize(800, 400);
                    bcm.setLocationRelativeTo(null);
                    bcm.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                    bcm.setVisible(true);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

        }
        System.exit(0);
    }
}
