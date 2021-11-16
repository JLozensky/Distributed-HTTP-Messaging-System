package ReceivingProgram;


import SharedLibrary.AbstractSqsInteractor;
import java.time.Instant;
import java.util.List;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;



/**
 * Singleton instance of an SQS client dedicated to receiving from an SQS queue
 */


public class SqsReceive extends AbstractSqsInteractor {

    private static final Integer BATCH_RECEIVE_NUM = 10;
    private static SqsReceive instance = new SqsReceive();

    public static SqsReceive getInstance() {
            return instance;
    }

    /**
     *  Attempts to receive a batch of messages from an SQS queue
     * @return the list of messages which can be null
     */
    public List<Message> receiveMessage(){
        // Format the receiveRequest with the queue's url the number of messages to receive and how long to wait for
        // a message to arrive
        ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
            .queueUrl(this.qUrl)
            .maxNumberOfMessages(BATCH_RECEIVE_NUM)
            .waitTimeSeconds(20)
            .build();

        // Measure how long the request takes, if it is in excess of the wait time print the number of messages
        // deleted thus far to the terminal
        Long start = Instant.now().toEpochMilli();
        List<Message> messageList = sqsClient.receiveMessage(receiveRequest).messages();
        Long stop = Instant.now().toEpochMilli();
        Long timeTaken = stop - start;
        if (timeTaken > 20000) {
            System.out.println("Num messages deleted = " + SqsDelete.getNumDeleted());
        }
        return messageList;
    }





}
