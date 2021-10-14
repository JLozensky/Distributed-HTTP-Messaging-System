package SharedLibrary;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class CachedThreadPool {
    private static CachedThreadPool instance = null;
    private ThreadPoolExecutor threadPoolExecutor;
    private static int NUM_THREADS = 10;

    private CachedThreadPool() {
//        this.threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(NUM_THREADS);
        this.threadPoolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    }
    public static CachedThreadPool getInstance(){
        if (instance == null){
            instance = new CachedThreadPool();
        }
        return instance;
    }

    public void runOnThread(Runnable runnable) {
        this.threadPoolExecutor.execute(runnable);
    }

    public int getQSize() {
        return this.threadPoolExecutor.getQueue().size();
    }

}

