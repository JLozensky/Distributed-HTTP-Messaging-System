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
 *
 *
 * SQS queue is being polled by receive program
 * receive program has multiple threads pulling messages
 * adds dedupe handle to thread safe set/hashmap
 * submits record to threadsafe file writer
 * on successful write to "database" receive handle is delivered for deletion
 *      need to see if deleting one at a time or in batches is better
 * on successful deletion from sqs we can remove msg handle from hashed set? Not sure if sqs promises to not resend
 * after deletion
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
        System.out.println("time taken: " + (stop - start) + "ms\n");
        System.out.println(messageList);
        return messageList;
    }





}
