package ReceivingProgram;

import SharedUtilities.AbstractSqsInteractor;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import software.amazon.awssdk.services.sqs.model.DeleteMessageBatchRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageBatchRequestEntry;
import software.amazon.awssdk.services.sqs.model.DeleteMessageBatchResponse;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageResponse;
import software.amazon.awssdk.services.sqs.model.Message;

/**
 * Creates and maintains the singleton SQS client used for deletion requests to the queue. Formats and executes the
 * deletion requests.
 *
 * Used this site to help in building the class
 * https://www.tabnine.com/code/java/methods/com.amazonaws.services.sqs.AmazonSQSClient/deleteMessageBatch
 *
 */
public class SqsDelete extends AbstractSqsInteractor {

    private static final SqsDelete instance = new SqsDelete(); // Singleton SQS instance for deletion
    private static final AtomicInteger numDeleted = new AtomicInteger(); // Tracker for the number of msgs deleted
    private static final AtomicInteger batchTotal = new AtomicInteger(); // Tracker for batches of messages
    private static final String queueUrl = instance.qUrl;

    /**
     * Method to submit a single message to the queue for deletion
     * @param receiptHandle unique identifier for the receive action that delivered the Message from the SQS queue
     * @return The response to the deletion request
     */
    public DeleteMessageResponse deleteMessage(String receiptHandle){
        // Build the deletion request
        DeleteMessageRequest request = DeleteMessageRequest.builder()
            .queueUrl(this.qUrl)
            .receiptHandle(receiptHandle)
            .build();

        // submit it to the queue and return the response
        return this.sqsClient.deleteMessage(request);
    }

    /**
     * Create a delete message batch request for an SQS queue from the given list of messages
     * @param messages the list of messages to request deletion of
     * @return the batch request instance created from the given messages
     */
    private static DeleteMessageBatchRequest makeRequest(ArrayList<Message> messages) {
        // an Array to hold the individual messages' deletion request
        ArrayList<DeleteMessageBatchRequestEntry> entryList = new ArrayList<>();

        // for each message create a DeleteMessageBatchRequestEntry instance and add it to the Array
        messages.forEach(message ->
                             entryList.add(
                                 DeleteMessageBatchRequestEntry
                                     .builder()
                                     .id(message.messageId())
                                     .receiptHandle(message.receiptHandle())
                                     .build()
                             )
        );
        // build the batch request from the list of entries and return
        return DeleteMessageBatchRequest.builder().queueUrl(queueUrl).entries(entryList).build();
    }



    /**
     * Intake for a list of messages that a deletion request should be made for
     * Also tracks the number of messages submitted for deletion for debugging purposes
     *
     * @param messages list of messages to be deleted
     * @return the response to the batch deletion request
     */
    public static DeleteMessageBatchResponse deleteMessages(ArrayList<Message> messages){
        if (messages.isEmpty()) { return null; }

        // add the number of messages in the list to a batching counter and once there are more than 100, transfer
        // the count into the total count of messages submitted for deletion
        if (batchTotal.addAndGet(messages.size()) > 100) {
            System.out.println("messages submitted for deletion= " + numDeleted.addAndGet(batchTotal.getAndSet(0)));
        }
        // Submit the deletion request to the SQS queue using a helper function to create the
        // DeleteMessageBatchRequest instance and return the response
        return instance.sqsClient.deleteMessageBatch(makeRequest(messages));
    }

    /**
     * Getter for the number of messages submitted for deletion
     * @return the total number of messages submitted during this run of the program
     */
    public static int getNumDeleted() {
        // Get any count that may be in batches and add it to the total before returning
        numDeleted.addAndGet(batchTotal.getAndSet(0));
        return numDeleted.get();
    }
}
