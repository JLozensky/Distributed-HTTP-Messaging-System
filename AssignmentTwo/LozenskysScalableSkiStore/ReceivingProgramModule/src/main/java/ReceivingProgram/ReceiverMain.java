package ReceivingProgram;

import java.util.List;
import software.amazon.awssdk.services.sqs.model.Message;

public class ReceiverMain {
    // main is endless loop grabbing messages
    public static void main(String[] args) {
        ThreadPool threadPool = ThreadPool.getInstance();
//        ThreadsafeFileWriter.setFilename("output");
//        ThreadsafeFileWriter.startInstance();
        int minDesiredRemainingCapacity = (int) Math.round(ThreadPool.getQCapacity() *.2);

        while (true){
            while (ThreadPool.getRemainingCapacity() > minDesiredRemainingCapacity) {
                List<Message> messageList = SqsReceive.getInstance().receiveMessage();
                if (messageList != null) {
                    threadPool.runThread(new MessageProcessor(messageList));
                }
            }
            try {
                System.out.println(SqsDelete.getNumDeleted());
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
