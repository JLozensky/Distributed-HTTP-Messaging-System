package ReceivingProgram;

import SharedUtilities.AbstractRecord;
import SharedUtilities.RecordCreationUtility;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import software.amazon.awssdk.services.sqs.model.Message;

public class MessageProcessor implements Runnable {
    private static Gson gson =  new Gson();
    private List<Message> messages;


    public MessageProcessor(List<Message> messages) {
        this.messages = messages;
    }

    /**
     * Runnable that processes a list of messages by using helper classes to check for duplicates, write to database,
     * and send the deletion request for the messages to an AWS SQS queue
     */
    @Override
    public void run() {
        // create an array to hold non-duplicate messages and records
        HashMap<AbstractRecord,Message> messagesToDelete = new HashMap<>();
        ArrayList<AbstractRecord> recordsToStore = new ArrayList<>();

        // for each message int the list
        for (Message m : this.messages) {

            // create an AbstractRecord data object from the json in the message
            AbstractRecord record = RecordCreationUtility.createRecordFromJson(m.body());

            // Check that the generated LiftRide contains valid values and check for duplicate messages
            if (record.isValid() && MessageStorage.insertData(record, m.messageId())) {

                // Add it to the array for non-duplicate messages
                recordsToStore.add(record);
                messagesToDelete.put(record,m);
            }
        }
        // Write all non-duplicate messages to the database then submit to queue for deletion
        int counter = 3;
        while (recordsToStore.size() > 0 && counter > 0) {
            DatabaseWriter.batchPut(recordsToStore);
            counter --;
        }
        if (!recordsToStore.isEmpty()){
            System.out.println("error in storing records to database list of un-stored records below\n");
            for (AbstractRecord r : recordsToStore){
                System.out.format("%s record failed to store in %s table\n", r.getRecordType(), r.getTableName());
                messagesToDelete.remove(r);
            }
        }
        SqsDelete.deleteMessages(new ArrayList<>(messagesToDelete.values()));
    }
}
