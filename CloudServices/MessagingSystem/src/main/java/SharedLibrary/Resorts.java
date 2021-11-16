package SharedLibrary;

import java.util.ArrayList;


public class Resorts {
    private ArrayList<ResortLite> resortsList;


    public Resorts(ArrayList<ResortLite> resortLites) {
        this.resortsList = resortLites;
    }

    public Resorts() {
        this.resortsList = new ArrayList<ResortLite>();
    }

    public ArrayList<ResortLite> getResorts(){
        return this.resortsList;
    }

    public void addResort(ResortLite resortLite){
        this.resortsList.add(resortLite);
    }

    public static Resorts makeDummyResorts(){
        Resorts resorts = new Resorts();
        for (int i=0; i < 42; i++) {
            resorts.addResort(new ResortLite("testResort", i));
        }
        return resorts;
    }
}
