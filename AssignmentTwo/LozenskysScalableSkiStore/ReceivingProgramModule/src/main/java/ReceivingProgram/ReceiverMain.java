package ReceivingProgram;

import java.util.List;
import software.amazon.awssdk.services.sqs.model.Message;

public class ReceiverMain {
    // main is endless loop grabbing messages
    public static void main(String[] args) {
        ThreadPool threadPool = ThreadPool.getInstance();
        while (true){
            List<Message> messageList = SqsReceive.getInstance().receiveMessage();
            if (messageList != null) {
                MessageProcessor processor = new MessageProcessor(messageList);
                threadPool.runThread(processor);
            }
        }

    }
}
