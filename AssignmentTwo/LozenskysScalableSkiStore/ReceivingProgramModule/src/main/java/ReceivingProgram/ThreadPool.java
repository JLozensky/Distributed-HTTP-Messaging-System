package ReceivingProgram;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A ThreadPool for executing Runnable instances, implemented following a singleton design pattern
 */
public class ThreadPool {
    private static ThreadPool instance = null;

    private static ThreadPoolExecutor threadPoolExecutor; // The executor that runs the thread pool
    private static final int NUM_THREADS = 20; // Max number of threads
    private static final int Q_CAPACITY = 50; // Max queue capacity for Runnable instances awaiting execution

    // bounded queue for storing Runnable instances
    private static final BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<>(Q_CAPACITY);


    /**
     * private generator of a ThreadPool instance that defines the min and max pool size, time to keep an idle thread
     * alive, and a bounded queue for holding Runnable instances awaiting a thread
     */

    private ThreadPool() {
        threadPoolExecutor = new ThreadPoolExecutor(4,NUM_THREADS,5, TimeUnit.SECONDS,blockingQueue);
    }

    /**
     * Gets the singleton instance of the threadpool, making one if it does not already exist
      * @return
     */
    public static ThreadPool getInstance() {
        if (instance == null){
            instance = new ThreadPool();
        }
        return instance;
    }

    /**
     * Getter for q capacity
     * @return the max number of Runnables the queue can hold
     */
    public static int getQCapacity() { return Q_CAPACITY; }

    /**
     * runs the submitted runnable on a thread in the pool
     * @param runnable the instance to run
     */
    public void runThread(Runnable runnable) {
        threadPoolExecutor.execute(runnable);
    }

    /**
     * Getter for remaining queue capacity
     * @return the current remaining capacity in the ThreadPool's queue
     */
    public static int getRemainingCapacity() { return blockingQueue.remainingCapacity(); }
}
