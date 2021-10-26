package Assignment2.ServerLibraryA2;

import java.util.ArrayList;
import java.util.Arrays;

public class Seasons {

    private ArrayList<String> seasons;

    public Seasons() {
        this.seasons = new ArrayList<>();
    }

    public Seasons(String[] seasons) {
        this.seasons = new ArrayList<>(Arrays.asList(seasons));
    }

    public ArrayList<String> getSeasons() {
        return seasons;
    }

    public void setSeasons(ArrayList<String> seasons) {
        this.seasons = seasons;
    }

    public static Seasons makeDummySeasons(){
        String[] seasons = {"1991", "1996", "2009", "2014"};
        return new Seasons(seasons);
    }
}
