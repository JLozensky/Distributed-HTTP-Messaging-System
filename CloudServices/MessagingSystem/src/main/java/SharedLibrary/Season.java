package SharedLibrary;

import Server.ContentValidationUtility;

public class Season implements InterfaceSkierDataObject {

    private String year;

    public Season(String year) {
        if (ContentValidationUtility.isSeason(year)){
            this.year = year;
        } else {
            this.year = null;
        }
    }

    public String getYear() {
        return year;
    }

    public boolean isValid() {
        return ContentValidationUtility.isSeason(this.year);
    }

    public void setYear(String year) {

        if(ContentValidationUtility.isSeason(year))
        {
        this.year = year;
        }
    }
}
