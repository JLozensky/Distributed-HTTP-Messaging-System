package SharedLibrary;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest;
import software.amazon.awssdk.services.sqs.model.PurgeQueueResponse;

/**
 * Learned how to do most of this from the various guides contained in the AWS documentation:
 * https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide
 */



// Need to figure out how to send messages to the vpc endpoint url instead of the sqs url

public abstract class AbstractSqsInteractor {

    protected  SqsClient sqsClient;
    protected  String qUrl;
    private String qName = "FirstSkiQueue";


    protected AbstractSqsInteractor(){
        this.sqsClient =  makeClient();
        this.qUrl = makeQUrl();
    }

    /**

     * @return a reference to the logged-in user's Queues
     */
    protected static SqsClient makeClient() {

        SqsClient client = SqsClient.builder()
            .region(Region.US_EAST_1)
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                client.close();
            }
        }));
        return client;
    }

    protected String makeQUrl() {

        GetQueueUrlResponse queueUrlResponse =
            this.sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(this.qName).build());
        return queueUrlResponse.queueUrl();


    }

    public PurgeQueueResponse purgeQ(){
        // send the signal to sqs to remove all messages from the Q
        PurgeQueueResponse purgeQueue =
            this.sqsClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(this.qUrl).build());

        // Amazon SQS documentation recommends giving an sqs queue 60 seconds to fully purge
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return purgeQueue;
    }

    private void ensureShutdown(){

    }

}
