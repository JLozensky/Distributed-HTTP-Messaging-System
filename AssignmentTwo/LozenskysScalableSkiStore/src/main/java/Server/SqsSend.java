package Server;

import SharedLibrary.AbstractSqsInteractor;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

public class SqsSend extends AbstractSqsInteractor {

    private static final SqsSend instance = new SqsSend();

    public static SqsSend getInstance() {
            return instance;
    }

    public boolean sendMessage(String json) {
        if (instance.sqsClient.sendMessage(SendMessageRequest.builder()
            .queueUrl(this.qUrl)
            .messageBody(json)
            .build()) != null) { return true; }
        else { return false; }
    }

}
