package SharedLibrary;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest;
import software.amazon.awssdk.services.sqs.model.PurgeQueueResponse;

public class AbstractSqsInteractor {
    protected static SqsClient sqsClient = makeClient();
    protected static String qUrl = makeQUrl();
    private static String qName = "FirstSkiQueue";




    public static SqsClient getSqsSendClient() {
        return sqsClient;
    }

    /**

     * @return a reference to the logged-in user's Queues
     */
    public static SqsClient makeClient() {

        return SqsClient.builder()
            .region(Region.US_EAST_1)
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }

    protected static String makeQUrl(){
        GetQueueUrlResponse queueUrlResponse =
            sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(qName).build());
        return queueUrlResponse.queueUrl();

    }

    public static PurgeQueueResponse purgeQ(){
        // send the signal to sqs to remove all messages from the Q
        PurgeQueueResponse purgeQueue = sqsClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(qUrl).build());

        // Amazon SQS documentation recommends giving an sqs queue 60 seconds to fully purge
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return purgeQueue;
    }

}
