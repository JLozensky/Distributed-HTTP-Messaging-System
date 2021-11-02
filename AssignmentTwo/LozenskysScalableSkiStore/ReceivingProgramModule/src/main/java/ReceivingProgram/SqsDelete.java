package ReceivingProgram;

import SharedLibrary.AbstractSqsInteractor;
import java.util.concurrent.atomic.AtomicInteger;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageResponse;

public class SqsDelete extends AbstractSqsInteractor {

    private static SqsDelete instance = null;
    private static AtomicInteger numDeleted = new AtomicInteger();


    public static SqsDelete getSqsDelete() {
        if (instance == null){
            return new SqsDelete();
        } else {
            return instance;
        }
    }

    public DeleteMessageResponse deleteMessage(String receiptHandle){
        if (numDeleted.incrementAndGet() % 100 == 0) {
            System.out.println("Msgs deleted = " + numDeleted.get());
        }
        return this.sqsClient.deleteMessage(
            DeleteMessageRequest.builder()
                .queueUrl(this.qUrl)
                .receiptHandle(receiptHandle)
                .build());
    }

    public static int getNumDeleted() {
        return numDeleted.get();
    }
}
