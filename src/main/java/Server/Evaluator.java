package Server;

import SharedLibrary.StatusCodes;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletResponse;

public abstract class Evaluator implements Runnable {
    protected HttpServletResponse response;
    protected String urlPath;
    protected PrintWriter printWriter;
    protected String MISSING_PARAM ="The request is missing parameters";
    protected String INVALID_PARAM = "The supplied parameters are invalid";
    protected String[] urlParts;
    protected Gson gson =new Gson();
    protected String body;

    public Evaluator(String urlPath, HttpServletResponse response){
        this.response = response;
        this.urlPath = urlPath;
        try {
            this.printWriter = response.getWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (this.urlPath == null || this.urlPath.isEmpty()) {
            this.urlParts = null;
        } else {
            this.urlParts = this.urlPath.split("/");
        }
    }

    public Evaluator(String urlPath, HttpServletResponse response, String body){
        this.response = response;
        this.body = body;
        this.urlPath = urlPath;
        try {
            this.printWriter = response.getWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (this.urlPath == null || this.urlPath.isEmpty()) {
            this.urlParts = null;
        } else {
            this.urlParts = this.urlPath.split("/");
        }
    }

    /**
     * Sets a 400 response code and sends the response to the original requester
     * @return false as this is an unsuccessful endpoint for an evaluation function
     */
    protected boolean errorInvalidParameters() {
        StatusCodes.setInvalidInputs(this.response);
        this.writeToClient(this.gson.toJson(new ErrorBuilder(this.INVALID_PARAM)));
        return false;
    }

    /**
     * Sets a 401 response code and sends the response to the original requester
     * @return false as this is an unsuccessful endpoint for an evaluation function
     */
    protected boolean errorMissingParameters() {
        StatusCodes.setIncorrectPath(this.response);
        this.writeToClient(this.gson.toJson(new ErrorBuilder(MISSING_PARAM)));
        return false;
    }


    protected void writeToClient(String text) {
        this.response.setContentType("application/json");
        this.printWriter.write(text);
        this.printWriter.flush();
    }

    protected void respondToClient() {
        this.printWriter.flush();
    }

    public HashMap<String,String> createQueryMap(String string) {
        if (string == null || string.isEmpty() || string.isBlank()){
            return null;
        }
        String[] queryParts = string.split("=");
        HashMap<String,String> queryMap = new HashMap<>();
        String curKey = "";
        for (int i = 0; i < queryParts.length; i++) {
            if (i % 2 == 0) {
                curKey = queryParts[i];
            } else {
                queryMap.put(curKey,queryParts[i]);
            }
        }
        return queryMap;
    }

    protected boolean validateUrlSize(int numSegmentsRequired) {
        if (urlParts == null) {return numSegmentsRequired == 0;}
        if (this.urlParts.length == numSegmentsRequired) { return true; }
        if (this.urlParts.length < numSegmentsRequired) { return this.errorMissingParameters(); }
        return this.errorInvalidParameters();
    }
}
