package ReceivingProgram;

import SharedLibrary.LiftRide;
import SharedLibrary.ResortLite;
import java.util.concurrent.ConcurrentHashMap;

public class MessageStorage {
    private static final ConcurrentHashMap<String, LiftRide> liftRideStorage =
        new ConcurrentHashMap();


    public static boolean insertData(ResortLite resort) {
        return false;
    }

    public static boolean insertData(LiftRide liftRide, String messageId) {
        try {
            if (!liftRideStorage.containsKey(messageId)) {
                liftRideStorage.put(messageId,liftRide);
            } else {
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
