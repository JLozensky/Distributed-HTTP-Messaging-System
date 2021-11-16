package ReceivingProgram;

import SharedLibrary.LiftRide;
import SharedLibrary.ResortLite;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Stores messages for concurrent de-duplication checks effectively acting like a cache of recently received messages
 */
public class MessageStorage {
    // maximum number of values to be stored for de-duplication checking
    private static final int Q_MAX = 500;

    // Hashmap to hold messageIds and corresponding data for fast de-duplication checks
    private static final ConcurrentHashMap<String, LiftRide> liftRideStorage =
        new ConcurrentHashMap(Q_MAX, 0.9f, 4);

    // Concurrent Queue to track Time To Live via a FIFO policy
    private static final ConcurrentLinkedQueue<String> queue = makeQ();

    /**
     * Helper function to make the q and fill it as well as the hashmap with initial dummy values to simplify
     * the insertion operation
     * @return
     */
    private static ConcurrentLinkedQueue<String> makeQ() {
        ConcurrentLinkedQueue<String> q = new ConcurrentLinkedQueue();
        for (int i = 0; i < Q_MAX; i++){
            q.add(String.valueOf(i));
            liftRideStorage.put(String.valueOf(i),new LiftRide(1,1));
        }
        return q;
    }

    /**
     * Placeholder for implementing insertion of resort data requests in the future
     * @param resort Data object to store
     * @param messageId Unique identifier for a given message
     * @return only false as this is a placeholder, in the future it will return false if the record is a duplicate,
     * otherwise true
     */
    public static boolean insertData(ResortLite resort, String messageId) {
        return false;
    }

    /**
     * Checks for duplicates and, if none, inserts a LiftRide record into the data storage
     * @param liftRide
     * @param messageId
     * @return
     */
    public static boolean insertData(LiftRide liftRide, String messageId) {
        try {
            // if the given message is not already stored in the hashmap
            if (!liftRideStorage.containsKey(messageId)) {
                // remove the oldest entry from the hashmap
                liftRideStorage.remove(queue.poll());

                // add the given message to the queue and hashmap
                queue.add(messageId);
                liftRideStorage.put(messageId,liftRide);
            } else {
                return false;
            }
        // if for any reason these operations fail return false as the record was not properly recorded
        } catch (Exception e) {
            return false;
        }
        return true;
    }

}
