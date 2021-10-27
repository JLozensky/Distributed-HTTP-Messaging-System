package ReceivingProgram;

import SharedLibrary.AbstractSqsInteractor;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageResponse;

public class SqsDelete extends AbstractSqsInteractor {

    public static DeleteMessageResponse deleteMessage(String receiptHandle){
        return sqsClient.deleteMessage(DeleteMessageRequest.builder().queueUrl(qUrl).receiptHandle(receiptHandle).build());
    }
}
