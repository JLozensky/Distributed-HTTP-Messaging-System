package Server;

import SharedUtilities.DatabaseInteractor;
import SharedUtilities.SkierRecord;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.internal.AttributeValues;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;


public class DatabaseReader extends DatabaseInteractor {




    private static final String RESORT = "resortID";
    private static final String SEASON = "seasonID";
    private static final String DAY = "dayID";
    private static final String COLON = ":";
    private static final String EXPRESSION_EQUALS = " = :";
    private static final String AND = " and ";
    private static HashMap<String, Integer> resortSkierCountCache = new HashMap<>();

    private static LinkedList<String> chacheResidencyQueue = new LinkedList<>();

    private static final String RESORT_INDEX = "resortID-instant-index";

    private static final SkierRecord SKIER_RECORD_REFERENCE = new SkierRecord();

    private static final DynamoDbTable<SkierRecord> SKIER_TABLE =
        dynamoEnhancedClient.table(SKIER_RECORD_REFERENCE.getTableName(), SKIER_RECORD_REFERENCE.getTableSchema());
    private static final DynamoDbIndex<SkierRecord> RESORT_TABLE = SKIER_TABLE.index(RESORT_INDEX);

    private static Expression buildExpression(String expressionString, Map<String, AttributeValue> valueMap){
        return Expression.builder().expression(expressionString).expressionValues(valueMap).build();
    }

    private static QueryEnhancedRequest makeSkierTotalVertDayRequest(int skierId, String resortID, String seasonID,
        String dayID){

        // Make the attributes the query will be filtered by
        AttributeValue resortValue = AttributeValues.stringValue(resortID);
        AttributeValue seasonValue = AttributeValues.stringValue(seasonID);
        AttributeValue dayValue = AttributeValues.stringValue(dayID);

        // build the condition that the partition key's value must equal the provided skierId
        QueryConditional condition = buildKeyCondition(skierId);


        // Set string values of expression, the first variable in each pairing is the field name in the database,
        // the second is the lookup key to get the value out of the expressionValues map below
        String expressionString = RESORT + EXPRESSION_EQUALS + RESORT + AND
                                      + SEASON + EXPRESSION_EQUALS + SEASON + AND
                                      + DAY + EXPRESSION_EQUALS + DAY;

        // Map the values that the above expression string will use to filter query results
        Map<String, AttributeValue> valueMap = Map.of(COLON+RESORT,resortValue, COLON + SEASON, seasonValue, COLON + DAY,
            dayValue);

        // Define the expressions to use in a query filter
        Expression expression = buildExpression(expressionString,valueMap);

        return buildRequest(condition, expression);

    }

    private static QueryEnhancedRequest makeSkierTotalVertRequest(int skierId, String resortID, String seasonID){
        // build the condition that the partition key's value must equal the provided skierId
        QueryConditional condition = buildKeyCondition(skierId);

        String expressionString = RESORT + EXPRESSION_EQUALS + RESORT;
        HashMap<String, AttributeValue> valueMap = new HashMap<>();
        valueMap.put(COLON+RESORT, AttributeValues.stringValue(resortID));

        if (seasonID != null){
            expressionString += AND + SEASON + EXPRESSION_EQUALS + SEASON;
            valueMap.put(COLON+SEASON, AttributeValues.stringValue(seasonID));
        }
        Expression expression = buildExpression(expressionString,valueMap);

        return buildRequest(condition,expression);

    }



    private static QueryEnhancedRequest buildRequest(QueryConditional conditional, Expression expression) {
        return QueryEnhancedRequest.builder().queryConditional(conditional).filterExpression(expression).build();
    }

    private static QueryConditional buildKeyCondition(int keyValue){
        return QueryConditional.keyEqualTo(Key.builder().partitionValue(keyValue).build());
    }

    private static QueryConditional buildKeyCondition(String keyValue){
        return QueryConditional.keyEqualTo(Key.builder().partitionValue(keyValue).build());
    }

    public static int getSkierTotalVert(String skierId, String resortID, String seasonID){
        PageIterable<SkierRecord> skierResults = SKIER_TABLE.query(
            makeSkierTotalVertRequest(Integer.valueOf(skierId) , resortID, seasonID));

        if (skierResults == null) { return 0; }

        return sumVert(skierResults);
    }

    public static int getSkierTotalVert(String skierId, String resortID) {
        return getSkierTotalVert(skierId,resortID,null);
    }

    private static int sumVert(PageIterable<SkierRecord> skierRecords){
        // make a container for the vert measure so it can be altered in a lambda expression
        ArrayList<Integer> totalVert = new ArrayList();
        totalVert.add(0);

        // iterate through each skierRecord adding its
        skierRecords.items().stream().forEach(record -> totalVert.set(0, totalVert.get(0) + record.getVert()));

        return totalVert.get(0);

    }

    public static int getSkierTotalVertDay(String skierId, String resortID, String seasonID, String dayID){

        PageIterable<SkierRecord> skierResults =
            SKIER_TABLE.query(makeSkierTotalVertDayRequest(Integer.valueOf(skierId),resortID,seasonID,dayID));

        if (skierResults == null){ return 0; }

        return sumVert(skierResults);
    }


    public static int getResortUniqueSkierDay(String resortID, String seasonID, String dayID){
        String cacheKey = resortID + COLON + dayID;

        // if resort is in the cache return the value otherwise proceed with the lookup
        if (resortSkierCountCache.containsKey(cacheKey)){
            return resortSkierCountCache.get(cacheKey);
        }

        HashSet<Integer> skierSet = new HashSet<>();

        SdkIterable<Page<SkierRecord>> resortResults = RESORT_TABLE.query(makeResortUniqueSkierDayRequest(resortID,
            seasonID,dayID));

        if (resortResults == null) { return 0; }

        resortResults.stream().forEach(
            skierRecordPage ->
                skierRecordPage.items().forEach(
                skierRecord -> skierSet.add(skierRecord.getSkierID())
            )
        );

        if (chacheResidencyQueue.size() >= 500){
            resortSkierCountCache.remove(chacheResidencyQueue.pop());
        }

        chacheResidencyQueue.add(cacheKey);
        resortSkierCountCache.put(cacheKey, skierSet.size());

        return skierSet.size();
    }

    private static QueryEnhancedRequest makeResortUniqueSkierDayRequest(String resortID, String seasonID,
        String dayID) {


        // Make the attributes the query will be filtered by
        AttributeValue seasonValue = AttributeValues.stringValue(seasonID);
        AttributeValue dayValue = AttributeValues.stringValue(String.valueOf(dayID));

        // build the condition that the partition key's value must equal the provided resortID
        QueryConditional condition = buildKeyCondition(resortID);

        // Set string values of expression, the first variable in each pairing is the field name in the database,
        // the second is the lookup key to get the value out of the expressionValues map below
        String expressionString = SEASON + EXPRESSION_EQUALS + SEASON + AND + DAY + EXPRESSION_EQUALS + DAY;

        // Map the values that the above expression string will use to filter query results
        Map<String, AttributeValue> valueMap = Map.of(COLON + SEASON, seasonValue, COLON + DAY, dayValue);

        // Define the expressions to use in a query filter
        Expression expression = buildExpression(expressionString,valueMap);

        return buildRequest(condition, expression);
    }
}
