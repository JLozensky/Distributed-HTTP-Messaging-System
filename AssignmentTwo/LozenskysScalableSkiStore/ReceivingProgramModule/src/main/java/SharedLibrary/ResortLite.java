package SharedLibrary;

import Server.ContentValidationUtility;

public class ResortLite implements InterfaceSkierDataObject {
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

    @Override
    public boolean isValid() {
        return ContentValidationUtility.isResortId(this.resortId)
                && ContentValidationUtility.isResortName(this.resortName);
    }
}
