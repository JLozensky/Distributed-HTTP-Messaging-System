package ReceivingProgram;

import SharedLibrary.AbstractSqsInteractor;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import software.amazon.awssdk.services.sqs.model.DeleteMessageBatchRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageBatchRequestEntry;
import software.amazon.awssdk.services.sqs.model.DeleteMessageBatchResponse;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageResponse;
import software.amazon.awssdk.services.sqs.model.Message;

public class SqsDelete extends AbstractSqsInteractor {

    private static final SqsDelete instance = new SqsDelete();
    private static AtomicInteger numDeleted = new AtomicInteger();
    private static AtomicInteger batchTotal = new AtomicInteger();
    private static String queueUrl = instance.qUrl;


    public DeleteMessageResponse deleteMessage(String receiptHandle){
        if (numDeleted.incrementAndGet() % 100 == 0) {
            System.out.println("Msgs deleted = " + numDeleted.get());
        }
        DeleteMessageRequest request = DeleteMessageRequest.builder()
            .queueUrl(this.qUrl)
            .receiptHandle(receiptHandle)
            .build();

        return this.sqsClient.deleteMessage(request);

    }

    private static DeleteMessageBatchRequest makeRequest(ArrayList<Message> messages) {
        ArrayList<DeleteMessageBatchRequestEntry> entryList = new ArrayList<>();
        messages.forEach(message ->
                             entryList.add(
                                 DeleteMessageBatchRequestEntry
                                     .builder()
                                     .id(message.messageId())
                                     .receiptHandle(message.receiptHandle())
                                     .build()
                             )
        );
        return DeleteMessageBatchRequest.builder().queueUrl(queueUrl).entries(entryList).build();
    }

    // got this function from here:
    // https://www.tabnine.com/code/java/methods/com.amazonaws.services.sqs.AmazonSQSClient/deleteMessageBatch
    public static DeleteMessageBatchResponse deleteMessages(ArrayList<Message> messages){
        if (messages.isEmpty()) { return null; }
        if (batchTotal.addAndGet(messages.size()) > 100) {
            System.out.println("submitted = " + numDeleted.addAndGet(batchTotal.getAndSet(0)));
        }

        return instance.sqsClient.deleteMessageBatch(makeRequest(messages));
    }

    public static int getNumDeleted() {
        return numDeleted.get();
    }
}
