package SharedUtilities;


import static software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags.primaryPartitionKey;
import static software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags.primarySortKey;
import static software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags.secondaryPartitionKey;
import static software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags.secondarySortKey;

import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticTableSchema;


public class SkierRecord extends AbstractRecord{
    private final RECORD_TYPE recordType = RECORD_TYPE.SKIER;
    private Integer skierID;
    private String resortID;
    private String seasonID;
    private String dayID;
    private String liftID;
    private Integer timeID;
    private Long instant = System.nanoTime();
    private Integer vert = 100; // for this part of the assignment we are assuming a vert of 100
    private static final StaticTableSchema<SkierRecord> SKIER_TABLE_SCHEMA= makeTableSchema();
    private static final String SKIER_TABLE_NAME = "SkiersResortToLifting";



    private static final String localIndexResort = "resortID-index";
    private static final String localIndexSeason = "seasonID-index";
    private static final String globalIndexResortInstant = "resortID-instant-index";


    public Integer getSkierID() {
        return this.skierID;
    }
    public Integer setSkierID(Integer skierID) {
        this.skierID = skierID;
        return this.skierID;
    }


    public Long getInstant() { return this.instant; }
    public Long setInstant(Long instant) {
        this.instant = instant;
        return this.instant;
    }


    public String getResortID() {
        return this.resortID;
    }
    public String setResortID(String resortID) {
        this.resortID = resortID;
        return this.resortID;
    }


    public String getSeasonID() {
        return this.seasonID;
    }
    public String setSeasonID(String seasonID) {
        this.seasonID = seasonID;
        return this.seasonID;
    }


    public String getDayID() {
        return this.dayID;
    }
    public String setDayID(String dayID) {
        this.dayID = dayID;
        return this.dayID;
    }


    public String getLiftID() { return this.liftID; }
    public String setLiftID(String liftID) {
        this.liftID = liftID;
        return this.liftID;
    }


    public Integer getTimeID() { return this.timeID; }
    public Integer setTimeID(Integer timeID) {
        this.timeID = timeID;
        return this.timeID;
    }


    public Integer getVert() {
        return this.vert;
    }
    public Integer setVert(Integer vert) {
        this.vert = vert;
        return this.vert;
    }




    private static StaticTableSchema<SkierRecord> makeTableSchema() {
        return StaticTableSchema.builder(SkierRecord.class)
            .newItemSupplier(SkierRecord::new)
            .addAttribute(Integer.class,
                a -> a.name("skierId")
                    .getter(SkierRecord::getSkierID)
                    .setter(SkierRecord::setSkierID)
                    .tags(
                        primaryPartitionKey(),
                        secondaryPartitionKey(localIndexResort),
                        secondaryPartitionKey(localIndexSeason)
                    ))

            .addAttribute(Long.class,
                a -> a.name("instant")
                    .getter(SkierRecord::getInstant)
                    .setter(SkierRecord::setInstant)
                    .tags(
                        primarySortKey(),
                        secondarySortKey(globalIndexResortInstant)
                    ))

            .addAttribute(String.class,
                a -> a.name("resortID")
                    .getter(SkierRecord::getResortID)
                    .setter(SkierRecord::setResortID)
                    .tags(
                        secondaryPartitionKey(globalIndexResortInstant),
                        secondarySortKey(localIndexResort)
                    ))

            .addAttribute(String.class,
                a -> a.name("seasonID")
                    .getter(SkierRecord::getSeasonID)
                    .setter(SkierRecord::setSeasonID)
                    .tags(secondarySortKey(localIndexSeason)))

            .addAttribute(String.class,
                a -> a.name("dayID")
                    .getter(SkierRecord::getDayID)
                    .setter(SkierRecord::setDayID)
            )

            .addAttribute(String.class,
                a -> a.name("liftID")
                    .getter(SkierRecord::getLiftID)
                    .setter(SkierRecord::setLiftID)
            )

            .addAttribute(Integer.class,
                a -> a.name("timeID")
                    .getter(SkierRecord::getTimeID)
                    .setter(SkierRecord::setTimeID)
            )

            .addAttribute(Integer.class,
                a -> a.name("vert")
                    .getter(SkierRecord::getVert)
                    .setter(SkierRecord::setVert)
            )
            .addAttribute(Long.class,
                a-> a.name("TTL")
                    .getter(SkierRecord::getTIME_TO_LIVE)
                    .setter(SkierRecord::setTIME_TO_LIVE)
            )
            .build();
    }

    @Override
    public RECORD_TYPE getRecordType() {
        return recordType;
    }

    @Override
    public StaticTableSchema getTableSchema() {
        return SKIER_TABLE_SCHEMA;
    }

    @Override
    public String getTableName() {
        return SKIER_TABLE_NAME;
    }

    @Override
    public boolean isValid() {
        return
            ContentValidationUtility.isSkier(this.skierID) &&
                ContentValidationUtility.isResortId(this.resortID) &&
                ContentValidationUtility.isSeason(this.seasonID) &&
                ContentValidationUtility.isDayId(this.dayID) &&
                ContentValidationUtility.isLiftId(this.liftID) &&
                ContentValidationUtility.isTime(this.timeID);
    }
}
