package SharedClientClasses;

import Client1.ClientOne;
import Client2.ClientTwo;
import Server.ContentValidationUtility;
import java.io.Console;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ArgParsingUtility {
    private static final int THREAD_MAX= 512;
    private static final int SKIER_MAX= 100000;
    private static final int LIFT_MIN = 5;
    private static final int LIFT_MAX = 60;
    private static final int LIFT_DEFAULT = 40;
    private static final int MEAN_LIFT_MAX = 20;
    private static final int MEAN_LIFT_DEFAULT=10;

    private static final HashMap<String, String> startingValues = new HashMap<>(){{
        this.put("-t",null);
        this.put("-s",null);
        this.put("-l",""+LIFT_DEFAULT);
        this.put("-ml",""+MEAN_LIFT_DEFAULT);
        this.put("-ip",null);
        this.put("-p",null);
    }};
    private static final HashMap<String, String> flagNames = new HashMap<>(){{
        this.put("-t","number of [t]hreads");
        this.put("-s","number of [s]kiers");
        this.put("-l","number of [l]ifts");
        this.put("-ml","[m]ean [l]ift rides per skier per day");
        this.put("-ip","[ip]address");
        this.put("-p","[p]ort number");
    }};
    private static final HashMap<String, String> errorMessages = new HashMap<>(){{
        this.put("-t","a number of threads between 1 and " + THREAD_MAX);
        this.put("-s","a number of skiers between 1 and " + SKIER_MAX);
        this.put("-l",
            "a number of lifts between " + LIFT_MIN + " and " + LIFT_MAX);
        this.put("-ml",
            "a number between 1 and " + MEAN_LIFT_MAX);
        this.put("-ip","a valid ip address");
        this.put("-p","a valid port number");
    }};
    private static final HashMap<String, String> idk = new HashMap<>(){{
        this.put("-t","");
        this.put("-s","");
        this.put("-l","");
        this.put("-ml","");
        this.put("-ip","");
        this.put("-p","");
    }};

    public static AbstractClient makeClient(String[] args, int clientSwitch){
        // Create default values list
        HashMap<String, String> clientValues = ArgParsingUtility.getStartingValues();

        // Iterate through the arguments
        boolean invalidValues = false;
        for (int i = 0; i < (args.length - 1); i++){
            // if the arg is a valid flag set the corresponding value in clientValues to the following arg
            if (clientValues.containsKey(args[i])){
                clientValues.put(args[i],args[i+1]);
                i++;
            } else {
                // if any invalid values are found flip to true
                invalidValues = true;
            }
        }
        // print usage message if any invalid values were entered
        if (invalidValues) {System.out.println(ArgParsingUtility.errorMessageGenerator());}

        Console console = System.console();
        if (console == null) {
//            System.out.println("This program was run from a non-interactive platform. If any of the required "
//                                   + "parameters are missing or incorrect the program will shut down");
        }

        // iterate through the flags, for each attempt to validate the corresponding value
        for (String flag : clientValues.keySet()){
            boolean validValue = ArgParsingUtility.validateEntry(flag, clientValues.get(flag));
            String temp;
            while (validValue == false) {
                System.out.println(ArgParsingUtility.errorMessageGenerator(flag, clientValues.get(flag)));
                clientValues.put(flag,console.readLine());
                validValue = ArgParsingUtility.validateEntry(flag,clientValues.get(flag));
            }
        }

        if (clientSwitch == 1) {
            return new ClientOne(Integer.parseInt(clientValues.get("-t")) ,Integer.parseInt(clientValues.get("-s")),
                Integer.parseInt(clientValues.get("-l")) ,Integer.parseInt(clientValues.get("-ml")),
                clientValues.get("-ip"), clientValues.get("-p"));
        } else {
            return new ClientTwo(Integer.parseInt(clientValues.get("-t")) ,Integer.parseInt(clientValues.get("-s")),
                Integer.parseInt(clientValues.get("-l")) ,Integer.parseInt(clientValues.get("-ml")),
                clientValues.get("-ip"), clientValues.get("-p"));

        }

    }


    private static HashMap<String,String> getStartingValues(){
        return startingValues;
    }

    private static String errorMessageGenerator(){
        StringBuilder sb = new StringBuilder();
        sb.append("One or more arguments were entered incorrectly.\nBelow is the full list of flags, only one "
                      + "argument is accepted per flag:\n\n");
        for (Map.Entry<String,String> entry : flagNames.entrySet()) {
            sb.append(entry.getKey() + ": " + entry.getValue() + '\n');
            sb.append(errorMessages.get(entry.getKey()) + "\n\n");
        }
        return sb.toString();
    }


    private static String errorMessageGenerator(String flag, String curArg){
        StringBuilder sb = new StringBuilder();
        if (curArg != null) {
            sb.append("The value ");
            sb.append(curArg);
            sb.append(" is invalid for the ");
        } else {
            sb.append("A value is required for the ");
        }
        sb.append(flagNames.get(flag));
        sb.append(". Please enter ");
        sb.append(errorMessages.get(flag));
        sb.append('\n');
        return sb.toString();
    }


    private static boolean validateEntry(String flag, String arg){
        switch(flag) {
            case "-t":
                return ContentValidationUtility.numInRange(arg,1,THREAD_MAX);
            case "-s":
                return ContentValidationUtility.numInRange(arg,1,SKIER_MAX); // numSkier
            case "-l":
                return ContentValidationUtility.numInRange(arg,LIFT_MIN,LIFT_MAX); // numLifts
            case "-ml":
                return ContentValidationUtility.numInRange(arg,1,MEAN_LIFT_MAX); // meanLifts
            case "-ip":
                return ContentValidationUtility.validateIP(arg); // iPAddress
            case "-p":
                return ContentValidationUtility.validatePort(arg);// port
            default:
                return false;
        }

    }

    public static String errorMessageGenerator(ArrayList<String> invalidParameters) {
        StringBuilder sb = new StringBuilder();


        return sb.toString();
    }

}
