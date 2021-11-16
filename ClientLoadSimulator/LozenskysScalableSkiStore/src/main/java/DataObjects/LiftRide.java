package DataObjects;

public class LiftRide {
    private Integer time;
    private Integer liftID;

    public LiftRide(Integer time, Integer liftID) {
        this.time = time;
        this.liftID = liftID;
    }

    public int getTime() {
        return this.time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getLiftID() {
        return this.liftID;
    }

    public void setLiftID(int liftID) {
        this.liftID = liftID;
    }

    public boolean isValid(){
        if (this.liftID == null || this.time == null){ return false; }
        return true;
    }
}
