package DataObjects;

public class ResortLite {
    private int resortId;
    private String resortName;

    public ResortLite(String resortName, int resortId) {
        this.resortName = resortName;
        this.resortId = resortId;
    }

    public String getResortName() {
        return this.resortName;
    }

    public int getResortId() {
        return this.resortId;
    }

    public void setResortName(String name) { this.resortName = name; }

    public void setResortId(int id) { this.resortId = id; }
}
