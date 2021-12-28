package SharedUtilities;

import static software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags.primaryPartitionKey;
import static software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags.primarySortKey;

import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticTableSchema;

public class ResortRecord extends AbstractRecord {
    private final RECORD_TYPE recordType = RECORD_TYPE.RESORT;
    private int resortID;
    private String seasonID;
    private Long instant = System.nanoTime();
    private static final String RESORTS_TABLE_NAME = "Resorts";
    private static final StaticTableSchema<ResortRecord> RESORT_TABLE_SCHEMA = makeResortTable();


    public int getResortID() {
        return this.resortID;
    }

    public int setResortID(int resortId) {
        this.resortID = resortId;
        return this.resortID;
    }

    public Long getInstant() { return this.instant; }
    public Long setInstant(Long instant) {
        this.instant = instant;
        return this.instant;
    }


    public String getSeasonID() {
        return this.seasonID;
    }

    public String setSeasonID(String season) {
        this.seasonID = season;
        return this.seasonID;
    }

    @Override
    public SharedUtilities.AbstractRecord.RECORD_TYPE getRecordType() {
        return recordType;
    }

    @Override
    public StaticTableSchema getTableSchema() {
        return RESORT_TABLE_SCHEMA;
    }

    @Override
    public String getTableName() {
        return RESORTS_TABLE_NAME;
    }

    private static StaticTableSchema<ResortRecord> makeResortTable() {
        return StaticTableSchema.builder(ResortRecord.class)
            .newItemSupplier(ResortRecord::new)
            .addAttribute(Integer.class,
                a -> a.name("resortID")
                    .getter(ResortRecord::getResortID)
                    .setter(ResortRecord::setResortID)
                    .tags(primaryPartitionKey()))
            .addAttribute(Long.class,
                a -> a.name("instant")
                    .getter(ResortRecord::getInstant)
                    .setter(ResortRecord::setInstant)
                    .tags(primarySortKey()))
            .addAttribute(String.class,
                a -> a.name("seasonID")
                    .getter(ResortRecord::getSeasonID)
                    .setter(ResortRecord::setSeasonID))
            .addAttribute(Long.class,
                a-> a.name("TTL")
                    .getter(ResortRecord::getTIME_TO_LIVE)
                    .setter(ResortRecord::setTIME_TO_LIVE)
            )
            .build();}

    @Override
    public boolean isValid() {
        return ContentValidationUtility.isSeason(this.seasonID) && ContentValidationUtility.isResortId(this.resortID);
    }

}