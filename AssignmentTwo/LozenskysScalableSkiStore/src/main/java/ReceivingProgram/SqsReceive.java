package ReceivingProgram;


import SharedLibrary.AbstractSqsInteractor;
import java.util.List;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

/**
 * Learned how to do most of this from the various guides contained in the AWS documentation:
 * https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide
 */

/**
 * Need to refactor so child classes get a client per class
 *
 * num responses against answer time for the bonus charts
 *
 * Server validates request
 *      thread pool? probs not the different servers are the thread pool
 * Runs logic to submit to SQS if valid
 *         need to provide dedupe id because otherwise hash is generated on content so msgs with the same content
 *         would fail to be delivered by the Q
 * Upon confirmed receipt send success response to client
 *
 *          also consider batching
 *
 *Split send receive into just send and then receive/delete classes
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

    public static List<Message> receiveMessage(){
        ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
            .queueUrl(qUrl)
            .maxNumberOfMessages(BATCH_RECEIVE_NUM)
            .build();
        List<Message> messageList = sqsClient.receiveMessage(receiveRequest).messages();
        return messageList;
    }



}
