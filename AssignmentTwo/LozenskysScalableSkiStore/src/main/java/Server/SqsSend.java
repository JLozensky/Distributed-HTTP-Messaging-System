package Server;

import SharedLibrary.AbstractSqsInteractor;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

public class SqsSend extends AbstractSqsInteractor {

    public static SendMessageResponse sendMessage(String json) {
        return sqsClient.sendMessage(SendMessageRequest.builder()
            .queueUrl(qUrl)
            .messageBody(json)
            .build());
    }


}
