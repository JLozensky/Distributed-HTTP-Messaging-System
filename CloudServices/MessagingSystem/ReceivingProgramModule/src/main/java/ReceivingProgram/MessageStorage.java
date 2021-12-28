package ReceivingProgram;

import SharedUtilities.AbstractRecord;
import SharedUtilities.ResortRecord;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Stores messages for concurrent de-duplication checks effectively acting like a cache of recently received messages
 */
public class MessageStorage {
    // maximum number of values to be stored for de-duplication checking
    private static final int Q_MAX = 500;

    // Hashmap to hold messageIds and corresponding data for fast de-duplication checks
    private static final ConcurrentHashMap<String, AbstractRecord> liftRideStorage =
        new ConcurrentHashMap(Q_MAX, 0.9f, 4);

    // Concurrent Queue to track Time To Live via a FIFO policy
    private static final ConcurrentLinkedQueue<String> queue = makeQ();

    /**
     * Helper function to make the q and fill it as well as the hashmap with initial dummy values to simplify
     * the insertion operation
     * @return the crafted queue
     */
    private static ConcurrentLinkedQueue<String> makeQ() {
        ConcurrentLinkedQueue<String> q = new ConcurrentLinkedQueue();
        for (int i = 0; i < Q_MAX; i++){
            q.add(String.valueOf(i));
            liftRideStorage.put(String.valueOf(i),new ResortRecord());
        }
        return q;
    }

    /**
     * Checks for duplicates and, if none, inserts a record into the data storage
     * @param record any object that extends the AbstractRecord class
     * @param messageId the unique message id provided by the SQS queue
     * @return true if the record didn't exist in the hashmap
     */
    public static boolean insertData(AbstractRecord record, String messageId) {
        try {
            // attempt to put the record in the Hashmap, the returned object is null if successful, otherwise returns
            // the value of the existing record tied to the given messageId

            AbstractRecord existingRecord = liftRideStorage.putIfAbsent(messageId,record);
            if (existingRecord == null){
                // remove the oldest entry from the hashmap
                liftRideStorage.remove(queue.poll());
                // add the given message to the queue
                queue.add(messageId);
                return true;
            } else {
                return false;
            }
        // if for any reason these operations fail return false as the record was not properly recorded
        } catch (Exception e) {
            return false;
        }
    }

}
