package Server;

import SharedLibrary.contentValidationUtility;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SkierGetEvaluator implements Runnable {

    private HttpServletResponse response;
    private HttpServletRequest request;
    private String resortId = null;
    private String seasonId = null;
    private String dayId = null;
    private String skierId = null;

    public SkierGetEvaluator(HttpServletRequest request, HttpServletResponse response){
        this.response = response;
        this.request = request;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used to create a thread, starting the thread
     * causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        this.response.setContentType("text/plain");
        String urlPath = this.request.getPathInfo();
        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            this.response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            this.sendError("missing parameters");
            return;
        }

        String[] urlParts = urlPath.split("/");
        // and now validate url path and return the response status code
        // (and maybe also some value if input is valid)

        if (!this.validURL(urlParts)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            // do any sophisticated processing with urlParts which contains all the url params
            // TODO: process url params in `urlParts`

        }
    }

    private boolean validURL(String[] urlParts) {
                if (urlParts.length < 2) {
                    sendError("missing parameters");
                }
                switch (urlParts[1]){
                    case "seasons":
                        if (validateVerticalGet(urlParts)){
                            doVerticalGet();
                        } else {
                            this.sendError("incorrect parameters");
                        }
                    case ""

                }

                return false;

    }

    private void doVerticalGet() {
    }

    public void setResortId(String resortId) {
        this.resortId = resortId;
    }

    public void setSeasonId(String seasonId) {
        this.seasonId = seasonId;
    }

    public void setDayId(String dayId) {
        this.dayId = dayId;
    }

    public void setSkierId(String skierId) {
        this.skierId = skierId;
    }

    private boolean validateVerticalGet(String[] urlParts) {
        int resortIdIndex = 0;
        int seasonIdIndex = 2;
        int daysText = 3;
        int dayIdIndex = 4;
        int skiersText = 5;
        int skierIdIndex = 6;

        if (
            contentValidationUtility.isResort(urlParts[resortIdIndex]) &&
            contentValidationUtility.isSeason(urlParts[seasonIdIndex]) &&
            urlParts[daysText].equals("days") &&
            contentValidationUtility.isDayId(urlParts[dayIdIndex]) &&
            urlParts[skiersText].equals("skiers") &&
            contentValidationUtility.isSkier(urlParts[skierIdIndex])
        ) {
            this.setResortId(urlParts[resortIdIndex]);
            this.setSeasonId(urlParts[seasonIdIndex]);
            this.setDayId(urlParts[dayIdIndex]);
            this.setSkierId(urlParts[skierIdIndex]);

            return true;
        }
        return false;
    }

    private void writeToClient(String text) {
        try {
            response.getWriter().write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendError(String text){
        try {
            this.response.getWriter().write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
