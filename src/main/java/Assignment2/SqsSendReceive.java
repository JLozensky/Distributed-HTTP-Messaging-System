package Assignment2;


import A1.ServerLibrary.LiftRide;
import com.google.gson.Gson;
import java.util.List;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest;
import software.amazon.awssdk.services.sqs.model.PurgeQueueResponse;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

/**
 * Learned how to do most of this from the various guides contained in the AWS documentation:
 * https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide
 */

/**
 * Server validates request
 *      thread pool? probs not the different servers are the thread pool
 * Runs logic to submit to SQS if valid
 *         need to provide dedupe id because otherwise hash is generated on content so msgs with the same contennt
 *         would fail to be delivered by the Q
 * Upon confirmed receipt send success response to client
 *
 *          also consider batching
 *
 *Split send receive into just send and then receive/delete classes
 *
 * SQS queue is being polled by receive program
 * receive program has multiple threads pulling messages
 * adds dedupe handle to thread safe set/hashmap
 * submits record to threadsafe file writer
 * on successful write to "database" receive handle is delivered for deletion
 *      need to see if deleting one at a time or in batches is better
 * on successful deletion from sqs we can remove msg handle from hashed set? Not sure if sqs promises to not resend
 * after deletion
 * keep last x number record files for debug purposes
 * will probably need to run tests independently which means storing results in file and modifying the logic on the
 * report builder
 */


public class SqsSendReceive {

    private static final Integer MAX_MESSAGES = 10;
    private static String qName = "FirstSkiQueue";
    private static SqsClient sqsSendClient = makeClient();
    private static SqsClient sqsReceiveClient = makeClient();
    private static SqsClient sqsDeleteClient = makeClient();
    private static String qUrl = makeQUrl();

    /**

     * @return a reference to the logged-in user's Queues
     */
    private static SqsClient makeClient() {

        return SqsClient.builder()
            .region(Region.US_EAST_1)
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }

    public static SqsClient getSqsSendClient() {
        return sqsSendClient;
    }

    private static String makeQUrl(){
            SqsClient client = getSqsSendClient();
            GetQueueUrlResponse queueUrlResponse =
                client.getQueueUrl(GetQueueUrlRequest.builder().queueName(qName).build());
            return queueUrlResponse.queueUrl();

    }

    public static void sendMessage(String json) {
        sqsSendClient.sendMessage(SendMessageRequest.builder()
            .queueUrl(qUrl)
            .messageBody(json)
            .build());
    }

    public static void deleteMessage(){
        sqsDeleteClient.deleteMessage(DeleteMessageRequest.builder().queueUrl(qUrl).receiptHandle())
    }

    public static List<Message> receiveMessage(){
        ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
            .queueUrl(qUrl)
            .maxNumberOfMessages(MAX_MESSAGES)
            .build();
        List<Message> messageList = sqsClient.receiveMessage(receiveRequest).messages();
        return messageList;
    }

    public static PurgeQueueResponse purgeQ(){}

    public static void main(String args[]) {
        LiftRide liftRide = new LiftRide(30,978);
        for (int i=0; i < 7; i++) {
            sendMessage();
        }
        List<Message> messageList = receiveMessage();
        for (int i = 0; i < messageList.size(); i++) {
            System.out.println(messageList.get(i).body());
        }
        System.out.println("second pull");


        sqsClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(qUrl).build());

        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }


}
