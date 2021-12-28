package SharedUtilities;

import SharedUtilities.AbstractRecord.RECORD_TYPE;
import com.google.gson.Gson;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecordCreationUtility {
    private static Gson gson =  new Gson();
    private static final String RECORD_TYPE_VALUE_REGEX = "\"recordType\":\"(.*?)\"";
    private static final Pattern RECORD_TYPE_PATTERN = Pattern.compile(RECORD_TYPE_VALUE_REGEX);
    private static final DatabaseInteractor dbReader = new DatabaseInteractor();

    public static ResortRecord createResortRecord(String[] urlParts, Season season){
        int RESORT_PART_INDEX = 1;
        ResortRecord resortRecord = new ResortRecord();
        resortRecord.setResortID(Integer.valueOf(urlParts[RESORT_PART_INDEX]));
        resortRecord.setSeasonID(season.getYear());
        return resortRecord;
    }

    public static SkierRecord createSkierRecord(String[] urlParts, LiftRide liftRide){
        SkierRecord record = new SkierRecord();
        if (buildSkierRecord(urlParts, record, liftRide)){
            return record;
        }
        return null;
    }

    private static boolean buildSkierRecord(String[] urlParts, SkierRecord record, LiftRide liftRide) {
        int resortIdIndex = 1;
        int seasonsText = 2;
        int seasonIdIndex = 3;
        int daysText = 4;
        int dayIdIndex = 5;
        int skiersText = 6;
        int skierIdIndex = 7;
        if (
                urlParts[seasonsText].equals("seasons") &&
                urlParts[daysText].equals("days") &&
                urlParts[skiersText].equals("skiers")
        ) {
            try {
                record.setResortID(urlParts[resortIdIndex]);
                record.setSeasonID(urlParts[seasonIdIndex]);
                record.setDayID(urlParts[dayIdIndex]);
                record.setSkierID(Integer.valueOf(urlParts[skierIdIndex]));
                record.setLiftID(String.valueOf(liftRide.getLiftID()));
                record.setTimeID(liftRide.getTime());
            } catch (Exception e) {
                return false;
            }
            return true;
        }
        return false;
    }

    public static AbstractRecord createRecordFromJson(String recordJson) {
        Matcher match = RECORD_TYPE_PATTERN.matcher(recordJson);
        match.find();
        String recordType = match.group(1);
        switch (RECORD_TYPE.valueOf(recordType)) {
            case RESORT:
                return gson.fromJson(recordJson,ResortRecord.class);
            case SKIER:
                return gson.fromJson(recordJson,SkierRecord.class);
            default:
                return null;
        }
    }
}
