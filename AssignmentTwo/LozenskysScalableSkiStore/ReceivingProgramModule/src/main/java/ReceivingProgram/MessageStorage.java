package ReceivingProgram;

import SharedLibrary.LiftRide;
import SharedLibrary.ResortLite;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MessageStorage {

    private static final int Q_MAX = 300;


    private static final ConcurrentHashMap<String, LiftRide> liftRideStorage =
        new ConcurrentHashMap(300, 0.9f, 4);

    private static final ConcurrentLinkedQueue<String> queue = makeQ();

    private static ConcurrentLinkedQueue<String> makeQ() {
        ConcurrentLinkedQueue<String> q = new ConcurrentLinkedQueue();
        for (int i = 0; i < Q_MAX; i++){
            q.add(String.valueOf(i));
            liftRideStorage.put(String.valueOf(i),new LiftRide(1,1));
        }
        return q;
    }


    public static boolean insertData(ResortLite resort) {
        return false;
    }

    public static boolean insertData(LiftRide liftRide, String messageId) {
        try {
            if (!liftRideStorage.containsKey(messageId)) {
                liftRideStorage.remove(queue.poll());
                queue.add(messageId);
                liftRideStorage.put(messageId,liftRide);
            } else {
                System.out.println("hit false");
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static int getDatapointCount(){
        return liftRideStorage.size();
    }

}
