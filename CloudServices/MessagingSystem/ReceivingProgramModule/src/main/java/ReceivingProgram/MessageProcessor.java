package ReceivingProgram;

import SharedLibrary.LiftRide;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import software.amazon.awssdk.services.sqs.model.Message;

public class MessageProcessor implements Runnable {
    private static Gson gson =  new Gson();
    private List<Message> messages;


    /**
     * Runnable that processes a list of messages by using helper classes to check for duplicates, write to csv file,
     * and send the deletion request for the messages to an AWS SQS queue
     */
    @Override
    public void run() {
        // create an array to hold non-duplicate messages
        ArrayList<Message> messagesToDelete = new ArrayList<>();

        // for each message int the list
        for (Message m : this.messages) {
            // create a LiftRide data object from the json in the message
            LiftRide liftRide = gson.fromJson(m.body(),LiftRide.class);

            // Check that the generated LiftRide contains valid values and check for duplicate messages
            if (liftRide.isValid() && MessageStorage.insertData(liftRide, m.messageId())) {

                // Submit the validated record to the file writer and add it to the array for non-duplicate messages
                ThreadsafeFileWriter.addRecord(m.body());
                messagesToDelete.add(m);
            }
        }
        // Delete all non-duplicate messages
        SqsDelete.deleteMessages(messagesToDelete);
    }

    public MessageProcessor(List<Message> messages) {
        this.messages = messages;
    }


}
