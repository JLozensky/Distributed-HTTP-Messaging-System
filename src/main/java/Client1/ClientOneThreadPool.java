package Client1;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ClientOneThreadPool {
    private static ClientOneThreadPool instance = null;
    private ThreadPoolExecutor threadPoolExecutor;



    private ClientOneThreadPool(int numThreads) {
        this.threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads);

    }
    public static ClientOneThreadPool getInstance(int numThreads){
        if (instance == null){
            instance = new ClientOneThreadPool(numThreads);
        }
        return instance;
    }

    public void run (Runnable runnable) {
        this.threadPoolExecutor.execute(runnable);
    }

    public int getQSize() {
        return this.threadPoolExecutor.getQueue().size();
    }

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
