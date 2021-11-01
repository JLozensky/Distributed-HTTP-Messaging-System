package SharedLibrary;

public class Resort implements InterfaceSkierDataObject {
    private int resortId;
    private String[] seasons;
    private String resortName;
    private int vertMeasure;

    // do std dist between 500 and 4500 for vertMeasure
    public Resort(String resortName, int resortId, String[] seasons, int vertMeasure) {
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

    @Override
    public boolean isValid() {
        return ContentValidationUtility.isResortId(this.resortId)
                && new Seasons(this.seasons).isValid()
                && ContentValidationUtility.isResortName(this.resortName)
                && ContentValidationUtility.isVertMeasure(this.vertMeasure);
    }
}