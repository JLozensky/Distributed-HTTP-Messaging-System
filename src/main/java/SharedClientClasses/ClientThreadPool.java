package SharedClientClasses;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Singleton thread pool so all requests are managed by the same Executor
 */
public class ClientThreadPool {
    private static ClientThreadPool instance = null;
    private ThreadPoolExecutor threadPoolExecutor;


    /**
     * Private constructor to facilitate singleton design
     * @param numThreads the number of threads to set as the max for the pool
     */
    private ClientThreadPool(int numThreads) {
        this.threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads);
    }

    /**
     * If no instance yet exists, instantiates and returns it, otherwise only changes the max threads on the existing
     * instance
     * @param numThreads max threads for the pool to potentially create
     * @return the singleton thread pool instance
     */
    public static ClientThreadPool getInstance(int numThreads){
        if (instance == null){
            instance = new ClientThreadPool(numThreads);
        } else {
            instance.threadPoolExecutor.setMaximumPoolSize(numThreads);
        }
        return instance;
    }

    /**
     * Executes the runnable on one of the threads in the pool
     * @param runnable
     */
    public void run (Runnable runnable) {
        this.threadPoolExecutor.execute(runnable);
    }

    /**
     * Provides a method to close the threadpool, waits for one minute to attempt to let threads complete before
     * terminating
     * @return
     */
    public boolean close(){
        this.threadPoolExecutor.shutdown();
        try {
            return this.threadPoolExecutor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
            this.threadPoolExecutor.shutdownNow();
        } finally {
            return this.threadPoolExecutor.isShutdown();
        }
    }
}
