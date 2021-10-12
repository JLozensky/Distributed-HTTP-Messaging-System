package SharedClientClasses;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ClientThreadPool {
    private static ClientThreadPool instance = null;
    private ThreadPoolExecutor threadPoolExecutor;



    private ClientThreadPool(int numThreads) {
        this.threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads);

    }
    public static ClientThreadPool getInstance(int numThreads){
        if (instance == null){
            instance = new ClientThreadPool(numThreads);
        } else {
            instance.threadPoolExecutor.setMaximumPoolSize(numThreads);
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
