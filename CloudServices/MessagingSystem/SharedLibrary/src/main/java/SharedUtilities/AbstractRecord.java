package SharedUtilities;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticTableSchema;


public abstract class AbstractRecord implements InterfaceSkierDataObject {

    public enum RECORD_TYPE{
        SKIER,
        RESORT
    }
    private long TIME_TO_LIVE = Instant.now().plus(4, ChronoUnit.HOURS).toEpochMilli()/1000;

    public long getTIME_TO_LIVE() {return this.TIME_TO_LIVE;}
    public long setTIME_TO_LIVE(long ttl) {
        this.TIME_TO_LIVE = ttl;
        return this.TIME_TO_LIVE;
    }

    public abstract RECORD_TYPE getRecordType();

    @Override
    public String toString(){
        return this.getClass().getSimpleName();
    }


    /**
     * Learned how to make implementations of this at the link below:
     * https://github.com/aws/aws-sdk-java-v2/blob/master/services-custom/dynamodb-enhanced/README.md
     *
     *
     */
    public abstract StaticTableSchema getTableSchema();

    public abstract String getTableName();
}
