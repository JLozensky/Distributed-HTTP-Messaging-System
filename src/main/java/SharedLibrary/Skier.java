package SharedLibrary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class Skier {
    private HashMap<Resort,ConcurrentHashMap<SkiDay,Integer>> liftRides;
    private int skierId;
    private Integer test;


    public Skier() {
        this.liftRides = null;
        this.test = null;


    }
}
