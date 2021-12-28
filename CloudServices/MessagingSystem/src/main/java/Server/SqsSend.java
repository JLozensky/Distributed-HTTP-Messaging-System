package Server;

import SharedUtilities.AbstractSqsInteractor;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

/**
 * Singleton SqsClient wrapper to send json messages to an SQS queue
 */
public class SqsSend extends AbstractSqsInteractor {

    // create singleton instance
    private static final SqsSend instance = new SqsSend();

    public static SqsSend getInstance() {
            return instance;
    }

    /**
     * Sends a given json message to the SQS queue targeted by the client
     * @param json
     * @return
     */
    public boolean sendMessage(String json) {
        if (instance.sqsClient.sendMessage(SendMessageRequest.builder()
            .queueUrl(this.qUrl)
            .messageBody(json)
            .build()) != null) { return true; }
        else { return false; }
    }

}
