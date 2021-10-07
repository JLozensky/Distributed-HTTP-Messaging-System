package Client1;

import Server.ThreadPool;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ClientOneThreadPool {
    private static ClientOneThreadPool instance = null;
    private ThreadPoolExecutor threadPoolExecutor;



    private ClientOneThreadPool(int numThreads) {
        // TODO write the instantiation of a thread pool
        this.threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads);

    }
    public static ClientOneThreadPool getInstance(int numThreads){
        if (instance == null){
            instance = new ClientOneThreadPool(numThreads);
        }
        return instance;
    }

    public void runOnPhaseOne(Runnable runnable) {
        this.threadPoolExecutor.execute(runnable);
    }

    public int getQSize() {
        return this.threadPoolExecutor.getQueue().size();
    }
}
