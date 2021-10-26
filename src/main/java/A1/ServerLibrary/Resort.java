package A1.ServerLibrary;

import java.util.ArrayList;

public class Resort {
    private int resortId;
    private ArrayList<String> seasons;
    private String resortName;
    private int vertMeasure;

    // do std dist between 500 and 4500 for vertMeasure
    public Resort(String resortName, int resortId, ArrayList<String> seasons, int vertMeasure) {
        this.resortName = resortName;
        this.resortId = resortId;
        this.seasons = seasons;
        this.vertMeasure = vertMeasure;
    }

    public String getResortName() {
        return this.resortName;
    }

    public int getResortId() {
        return this.resortId;
    }
}