package A1.ServerLibrary;

public class SkiDay {
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
}
