package SharedLibrary;

public class LiftRide {
    private int time;
    private int liftID;

    public LiftRide(int time, int liftID) {
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
}
