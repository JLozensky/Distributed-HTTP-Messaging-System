package Client1;

import java.io.Console;
import java.util.HashMap;
import java.util.Map;

public class Client {




    private int numThreads;
    private int numSkiers;
    private int numLifts;
    private int meanLiftPerSkier;
    private String ipAddress;
    private String port;


    public Client(String numThreads, String numSkiers, String numLifts, String meanLiftPerSkier,
        String ipAddress, String port){
        this.numThreads = Integer.parseInt(numThreads);
        this.numSkiers = Integer.parseInt(numSkiers);
        this.numLifts = Integer.parseInt(numLifts);
        this.meanLiftPerSkier = Integer.parseInt(meanLiftPerSkier);
        this.ipAddress = ipAddress;
        this.port = port;
    }

    @Override
    public String toString() {
        return "Client{" +
                   "numThreads=" + numThreads +
                   ", numSkiers=" + numSkiers +
                   ", numLifts=" + numLifts +
                   ", meanLiftPerSkier=" + meanLiftPerSkier +
                   ", ipAddress='" + ipAddress + '\'' +
                   ", port='" + port + '\'' +
                   '}';
    }

    public static void main(String args[]){
        Client client= ArgParsingUtility.makeClient(args);
        System.out.println(client.toString());
    }
}
