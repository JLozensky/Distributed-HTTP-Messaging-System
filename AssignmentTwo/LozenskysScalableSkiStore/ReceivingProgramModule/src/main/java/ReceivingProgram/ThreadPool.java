package ReceivingProgram;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool {
    private static ThreadPool instance = null;

    private static ThreadPoolExecutor threadPoolExecutor;
    private static int NUM_THREADS = 20;
    private static int Q_CAPACITY = 50;
    private static BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<>(Q_CAPACITY);

    private ThreadPool() {
        this.threadPoolExecutor = new ThreadPoolExecutor(4,NUM_THREADS,5, TimeUnit.SECONDS,blockingQueue);
            }

    public static ThreadPool getInstance() {
        if (instance == null){
            instance = new ThreadPool();
        }
        return instance;
    }

    public static int getQCapacity() { return Q_CAPACITY; }

    public void runThread(Runnable runnable) {
        this.threadPoolExecutor.execute(runnable);
    }

    public static int getRemainingCapacity() { return blockingQueue.remainingCapacity(); }
}
