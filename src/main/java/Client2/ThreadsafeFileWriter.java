package Client2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadsafeFileWriter {

    private static ConcurrentLinkedQueue<String> queue;
    private static ThreadsafeFileWriter instance = null;
    private static BufferedWriter writer;
    private static Thread thread;
    private static ReentrantLock writeLock;
    private static ReentrantLock createDestroyLock = new ReentrantLock();
    private static String filename = "output.txt";


    private static void initVariables() {
        queue = new ConcurrentLinkedQueue<>();
        writeLock = new ReentrantLock();
        try {
            // will create output file if one doesn't exist in the current directory
            File file = new File(filename);
            file.createNewFile();

            // fileWriter will always overwrite a file of the given name
            writer = new BufferedWriter(new FileWriter(filename, false));

            writeToFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean setFilename(String string) {
        if (instance == null && createDestroyLock.tryLock()) {
                filename = string + ".txt";
                createDestroyLock.unlock();
        }
        return false;
    }

    public static ThreadsafeFileWriter startInstance(){
        if (instance == null) {
            createDestroyLock.lock();
            ThreadsafeFileWriter.initVariables();
            createDestroyLock.unlock();

        }
        return instance;
    }

    public static void addRecord(String s){
        if (queue == null) {return;}
        queue.add(s);
    }
    private static String pollQ(){
        return queue.poll();
    }

    private static synchronized void writeToFile() {
        thread = new Thread(
            ()->{

                while (true){
                String record = pollQ();
                if (record == null) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        break;
                    }
                }

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
        thread.start();
    }

    public static void finish() {
        // lock the create destroy lock while transitioning states
        createDestroyLock.lock();

        // get the reference to the current queue and then set it to null so no more can be added
//        ConcurrentLinkedQueue<String> localQ = queue;

        // set the current instance to null so that a new one will be created the next time get instance is called
        instance = null;

        // wait for any threads trying to instantiate a write-thread to finish doing so
        writeLock.lock();


        try {
            // if the current write thread is alive wait for it to die
            if (thread.isAlive()) {
                thread.interrupt();
                thread.join();
            }
            // if there is anything left in the old Q finish flushing to file
            if (queue.peek() != null){
                writeToFile();
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
