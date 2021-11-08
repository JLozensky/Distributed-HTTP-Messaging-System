package ReceivingProgram;


import SharedLibrary.AbstractSqsInteractor;
import java.time.Instant;
import java.util.List;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;



/**
 * num responses against answer time for the bonus charts TODO edit client
 *
 *
 * Consider batching
 * keep last x number record files for debug purposes
 * will probably need to run tests independently which means storing results in file and modifying the logic on the
 * report builder
 */


public class SqsReceive extends AbstractSqsInteractor {

    private static final Integer BATCH_RECEIVE_NUM = 10;
    private static SqsReceive instance = null;

    public static SqsReceive getInstance() {
        if (instance == null) {
            return new SqsReceive();
        } else {
            return instance;
        }
    }

    public List<Message> receiveMessage(){
        ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
            .queueUrl(this.qUrl)
            .maxNumberOfMessages(BATCH_RECEIVE_NUM)
            .waitTimeSeconds(20)
            .build();
        Long start = Instant.now().toEpochMilli();
        List<Message> messageList = sqsClient.receiveMessage(receiveRequest).messages();
        Long stop = Instant.now().toEpochMilli();
        Long timeTaken = stop - start;
        if (timeTaken > 20000) {
            System.out.println("Num messages deleted = " + SqsDelete.getNumDeleted());
        }
        return messageList;
    }





}
