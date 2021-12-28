package Server;

public class ResortGetUniqueSkiers {
    private String resortID;
    private int numSkiers;

    public ResortGetUniqueSkiers(String resortID, int numSkiers) {
        this.resortID = resortID;
        this.numSkiers = numSkiers;
    }

    public String getResortID() {
        return resortID;
    }

    public void setResortID(String resortID) {
        this.resortID = resortID;
    }

    public int getNumSkiers() {
        return numSkiers;
    }

    public void setNumSkiers(int numSkiers) {
        this.numSkiers = numSkiers;
    }
}
