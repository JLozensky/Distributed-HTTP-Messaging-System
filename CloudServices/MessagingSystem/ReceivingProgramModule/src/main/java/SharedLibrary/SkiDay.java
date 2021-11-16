package SharedLibrary;

public class SkiDay implements InterfaceSkierDataObject {
    String season;
    Integer dayId;

    public SkiDay(String season, int dayId) {
        this.season = season;
        this.dayId = dayId;
    }

    @Override
    public boolean equals(Object o){
        if (o == this) {
            return true;
        }
        if (!(o instanceof SkiDay)) {
            return false;
        }
        SkiDay sd = (SkiDay) o;
        return this.season.equals(sd.season) && this.dayId.equals(sd.dayId);
    }

    @Override
    public boolean isValid() {
        return ContentValidationUtility.isSeason(this.season)
                && ContentValidationUtility.isDayId(this.dayId);

    }
}
