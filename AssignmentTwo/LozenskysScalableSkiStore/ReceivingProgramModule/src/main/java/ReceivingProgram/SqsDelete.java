package ReceivingProgram;

import SharedLibrary.AbstractSqsInteractor;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageResponse;

public class SqsDelete extends AbstractSqsInteractor {

    private static SqsDelete instance = null;


    public static SqsDelete getSqsDelete() {
        if (instance == null){
            return new SqsDelete();
        } else {
            return instance;
        }
    }

    public DeleteMessageResponse deleteMessage(String receiptHandle){
        return this.sqsClient.deleteMessage(
            DeleteMessageRequest.builder()
                .queueUrl(this.qUrl)
                .receiptHandle(receiptHandle)
                .build());
    }
}
