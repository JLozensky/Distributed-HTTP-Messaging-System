package ReceivingProgram;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Uses a concurrent linked queue for a many to one producer to consumer filewriter maintaining a singleton static
 * instance to make it easy for all threads to post records to it
 */

public class ThreadsafeFileWriter {

    private static ConcurrentLinkedQueue<String> queue;
    private static ThreadsafeFileWriter instance = null;
    private static BufferedWriter writer;
    private static Thread thread;

    // lock synchronizing the creation and destruction of the filewriter instance
    private static ReentrantLock createDestroyLock = new ReentrantLock();

    // default filename
    private static String filename = "output.csv";


    /**
     * Initialize variables for the singleton instance of the writer, only called from within the createDestroyLock
     */
    private static void initVariables() {
        // Unbounded queue to hold records from producers
        queue = new ConcurrentLinkedQueue<>();

        try {
            // will create output file if one doesn't exist in the current directory
            File file = new File(filename);
            file.createNewFile();

            // fileWriter will always overwrite a file of the given name
            writer = new BufferedWriter(new FileWriter(filename, false));

            // starts the consumer thread
            writeToFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the filename for the output if the writer instance has not yet started
     * @param string name for the output file
     * @return true if name was successfully set
     */
    public static boolean setFilename(String string) {
        // if no instance exists block any creation until after the given filename is set
        createDestroyLock.lock();
        if (instance == null) {

                filename = string + ".csv";
                createDestroyLock.unlock();
                return true;
        }
        createDestroyLock.unlock();
        return false;
    }

    /**
     * Start the singleton instance of the fileWriter, do nothing if one already running
     */
    public static void startInstance(){
        createDestroyLock.lock();
        if (instance == null) {
            ThreadsafeFileWriter.initVariables();
        }
        createDestroyLock.unlock();
    }

    /**
     * Method for producers to add a record to the shared queue
     * @param s String record to be added
     */
    public static void addRecord(String s){
        // if no queue exists there is no filewriter instance currently accepting records so return otherwise add
        // record
        if (queue == null) {return;}
        queue.add(s);
    }

    /**
     * Method for the single consumer thread to pull records off the shared queue
     * @return
     */
    private static String pollQ(){
        return queue.poll();
    }

    /**
     * private instance that creates the consumer thread that asynchronously writes to a file from the queue
     */
    private static synchronized void writeToFile() {
        thread = new Thread(
            ()->{
                // endless loop that is only stopped on interruption from the finish() method
                while (true){
                    // try to get a record if one doesn't exist, wait a second and try again. Break on interrupt()
                    String record = pollQ();
                    if (record == null) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                        // while there are records in the queue remove them and write to file
                        while(record != null) {
                            try {
                                writer.write(record);
                                writer.newLine();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            record = pollQ();
                    }
                }
            }
        );
        // start the thread
        thread.start();
    }


    /**
     * Closes and nullifies the current writer instance and blocks waiting on all records to finish recording
     */
    public static void finish() {
        // lock the create destroy lock while transitioning states
        createDestroyLock.lock();


        // set the current instance to null so that a new one will be created the next time get instance is called
        instance = null;

        try {
            // while there are still entries in the queue wait for them to finish
            while(queue.size() > 0) {
                Thread.sleep(1000);
            }
            // if the current write thread is alive interrupt it and wait for it to die
            if (thread.isAlive()) {
                thread.interrupt();
                thread.join();
            }

        } catch (InterruptedException e) {}

        // close the writer object
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // unlock so a new writer can be instantiated
        createDestroyLock.unlock();
    }
}
