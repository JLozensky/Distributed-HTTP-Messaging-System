package SharedLibrary;

import java.util.ArrayList;

public class Resorts {
    private ArrayList<Resort> resortsList;


    public Resorts(ArrayList<Resort> resorts) {
        this.resortsList = resorts;
    }

    public Resorts() {
        this.resortsList = new ArrayList<Resort>();
    }

    public ArrayList<Resort> getResorts(){
        return this.resortsList;
    }

    public void addResort(Resort resort){
        this.resortsList.add(resort);
    }
}
