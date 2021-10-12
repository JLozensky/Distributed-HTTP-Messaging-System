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


    private ThreadsafeFileWriter() {
        queue = new ConcurrentLinkedQueue<>();
        writeLock = new ReentrantLock();
        try {
            // will create output file if one doesn't exist in the current directory
            File file = new File(filename);
            file.createNewFile();

            // fileWriter will always overwrite a file of the given name
            writer = new BufferedWriter(new FileWriter(filename, false));

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

    public static ThreadsafeFileWriter getInstance(){
        if (instance == null) {
            createDestroyLock.lock();
            instance = new ThreadsafeFileWriter();
            createDestroyLock.unlock();
        }
        return instance;
    }

    public static void addRecord(String s){
        if (queue == null) {return;}
        queue.add(s);
        if (!thread.isAlive() && writeLock.tryLock()) {
            writeToFile(queue);
            writeLock.unlock();
        }
    }

    private synchronized static void writeToFile(ConcurrentLinkedQueue<String> q) {
        thread = new Thread(
            ()->{
                ConcurrentLinkedQueue<String> localReferenceQ = q;
                if (localReferenceQ == null) { return; }
                String record = localReferenceQ.poll();
                while(record != null) {

                    try {
                        writer.write(record);
                        writer.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    record = localReferenceQ.poll();
                }
            }
        );
        thread.start();
    }

    public static void finish() {
        // lock the create destroy lock while transitioning states
        createDestroyLock.lock();

        // get the reference to the current queue and then set it to null so no more can be added
        ConcurrentLinkedQueue<String> localQ = queue;
        queue = null;

        // set the current instance to null so that a new one will be created the next time get instance is called
        instance = null;

        // wait for any threads trying to instantiate a write-thread to finish doing so
        writeLock.lock();


        try {
            // if the current write thread is alive wait for it to die
            if (thread.isAlive()) {
                thread.join();
            }
            // if there is anything left in the old Q finish flushing to file
            if (localQ.peek() != null){
                writeToFile(localQ);
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
