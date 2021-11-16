package SharedLibrary;

import Server.ContentValidationUtility;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class Skier implements InterfaceSkierDataObject {
    private HashMap<ResortLite,ConcurrentHashMap<SkiDay,Integer>> liftRides;
    private int skierId;



    public Skier() {
        this.liftRides = null;



    }

    @Override
    public boolean isValid() {
        return ContentValidationUtility.isSkier(this.skierId);
    }
}
