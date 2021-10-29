package Server;

import SharedLibrary.AbstractSqsInteractor;
import java.util.concurrent.atomic.AtomicInteger;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

public class SqsSend extends AbstractSqsInteractor {

    private static SqsSend instance = null;
    private static AtomicInteger atomicInteger;

    private SqsSend() {
        super();
        atomicInteger = new AtomicInteger();
    }

    public static SqsSend getInstance() {
        if (instance == null) {
            return new SqsSend();
        } else {
            return instance;
        }
    }

    public boolean sendMessage(String json) {
        if (this.sqsClient.sendMessage(SendMessageRequest.builder()
            .queueUrl(this.qUrl)
            .messageBody(json)
            .build()) instanceof SendMessageResponse) { return true; }
        else { return false; }
    }

}
