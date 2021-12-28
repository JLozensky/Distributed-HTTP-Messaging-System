package ReceivingProgram;

import software.amazon.awssdk.services.sqs.model.Message;

import java.util.List;

/**
 * An endless loop grabbing messages from an SQS queue as the main function for the ReceivingProgramModule
 */
public class ReceiverMain {

    public static void main(String[] args) {
        // Get the reference to the program's singleton ThreadPool instance
        ThreadPool threadPool = ThreadPool.getInstance();


        // Set target queue capacity to 20% of the threadpool's max capacity
        int minDesiredRemainingCapacity = (int) Math.round(ThreadPool.getQCapacity() *.2);

        // Search for messages as long as the program is running
        while (true){
            // While there is greater than 20% capacity remaining in the threadpool's work queue
            while (ThreadPool.getRemainingCapacity() > minDesiredRemainingCapacity) {
                // Try to receive a list of messages from an SQS queue
                List<Message> messageList = SqsReceive.getInstance().receiveMessage();

                // If messages are returned, add a MessageProcessor Runnable to the thread pool's work queue
                if (messageList != null) {
                    threadPool.runThread(new MessageProcessor(messageList));
                }
            }
            // This is only hit when either the threadpool's queue exceeds 80% capacity
            // Print the number of messages deleted thus far (debugging purposes) and wait for one second
            try {
                System.out.println(SqsDelete.getNumDeleted());
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }
}
