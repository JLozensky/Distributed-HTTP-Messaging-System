package Assignment2.ServerA2;

import javax.servlet.http.HttpServletResponse;

public final class StatusCodesA2 {

    private StatusCodesA2(){}

    public static void setRequestSuccess(HttpServletResponse response){
        response.setStatus(response.SC_OK);
    }

    public static void setWriteSuccess(HttpServletResponse response){
        response.setStatus(response.SC_CREATED);
    }

    public static void setInvalidInputs(HttpServletResponse response){
        response.setStatus(response.SC_BAD_REQUEST);
    }

    public static void setIncorrectPath(HttpServletResponse response){
        response.setStatus(response.SC_NOT_FOUND);
    }
}
