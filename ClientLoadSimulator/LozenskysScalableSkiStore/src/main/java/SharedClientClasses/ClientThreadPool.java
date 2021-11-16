package SharedClientClasses;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Singleton thread pool so all requests are managed by the same Executor
 */
public class ClientThreadPool {
    private ClientThreadPool instance = null;
    private ThreadPoolExecutor threadPoolExecutor;


    /**
     * Thread pool to run on
     * @param numThreads the number of threads to set as the max for the pool
     */
    ClientThreadPool(int numThreads) {
        this.threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads);
    }

    /**
     * Executes the runnable on one of the threads in the pool
     * @param runnable
     */
    public void run (Runnable runnable) {
        this.threadPoolExecutor.execute(runnable);
    }

    /**
     * Provides a method to close the thread pool, waits for one minute to attempt to let threads complete before
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
