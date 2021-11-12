package Server;

import SharedLibrary.AbstractSqsInteractor;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

public class SqsSend extends AbstractSqsInteractor {

    private static SqsSend instance = new SqsSend();

    private SqsSend() {
        super();
    }

    public static SqsSend getInstance() {
            return instance;
    }

    public boolean sendMessage(String json) {
        if (this.sqsClient.sendMessage(SendMessageRequest.builder()
            .queueUrl(this.qUrl)
            .messageBody(json)
            .build()) instanceof SendMessageResponse) { return true; }
        else { return false; }
    }

}
