package ReceivingProgram;

import SharedUtilities.AbstractRecord;
import SharedUtilities.DatabaseInteractor;
import java.util.ArrayList;
import java.util.HashMap;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteResult;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;

public class DatabaseWriter extends DatabaseInteractor {


    public static boolean batchPut(ArrayList<AbstractRecord> recordsList){
        int MAX_TRIES = 6;
        int waitMultiplier = 0;

        batchWrite(recordsList);

        while (! recordsList.isEmpty()){
            // Sleep using exponential backoff starting at 100ms and trying 6 times leading to the longest wait being
            // 100 ms * 2^5 =  3,200ms
            try {
                Thread.sleep((long) (100 * Math.pow(2,waitMultiplier)));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            waitMultiplier ++;
            // set the resorts list to only the failed records
            if (waitMultiplier == MAX_TRIES - 1) {return false;}

            batchWrite(recordsList);
        }
        return true;
    }

    private static void batchWrite(ArrayList<AbstractRecord> recordsList) {
        if (recordsList.isEmpty()) {return;}

        HashMap<String, DynamoDbTable<AbstractRecord>> tableHashMap = new HashMap<>();
        // create the batch request builder
        BatchWriteItemEnhancedRequest.Builder batchRequestBuilder = BatchWriteItemEnhancedRequest.builder();

        // for each resort in the list
        for (AbstractRecord r : recordsList){

            Class recordClass = r.getClass();
            if (! tableHashMap.containsKey(r.getTableName())) {
                tableHashMap.put(r.getTableName(),dynamoEnhancedClient.table(r.getTableName(), r.getTableSchema()));
            }

            // add a new WriteBatch object containing the resort to the batch request builder
            batchRequestBuilder.addWriteBatch(
                WriteBatch.builder(recordClass)
                    .mappedTableResource(tableHashMap.get(r.getTableName()))
                    .addPutItem(PutItemEnhancedRequest.builder(recordClass).item(r).build())
                    .build());
        }

        // build the batch request
        BatchWriteItemEnhancedRequest request = batchRequestBuilder.build();

        BatchWriteResult result = dynamoEnhancedClient.batchWriteItem(request);
        recordsList.clear();

        for (DynamoDbTable dbTable : tableHashMap.values()){
            recordsList.addAll(result.unprocessedPutItemsForTable(dbTable));
        }
    }
}
