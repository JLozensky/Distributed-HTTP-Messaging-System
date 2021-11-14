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
     * When an object implementing interface {@code Runnable} is used to create a thread, starting the thread causes the
     * object's {@code run} method to be called in that separately executing thread.
     * <p>
     * The general contract of the method {@code run} is that it may take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        ArrayList<Message> messagesToDelete = new ArrayList<>();
        for (Message m : this.messages) {
            LiftRide liftRide = gson.fromJson(m.body(),LiftRide.class);
            if (liftRide.isValid() && MessageStorage.insertData(liftRide, m.messageId())) {
                ThreadsafeFileWriter.addRecord(m.body());
                messagesToDelete.add(m);
            }
        }
        SqsDelete.deleteMessages(messagesToDelete);
    }

    public MessageProcessor(List<Message> messages) {
        this.messages = messages;
    }


}
