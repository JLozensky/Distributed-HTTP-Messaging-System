package Assignment2.ServerLibraryA2;


import Assignment2.ServerA2.ContentValidationUtilityA2;

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
        if (
            ! ContentValidationUtilityA2.isLiftId(this.liftID) ||
            ! ContentValidationUtilityA2.isTime(this.time)
        ) { return false; }
        return true;
    }
}
