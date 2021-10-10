package Server;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPool {
    private static ThreadPool instance = null;
    private ThreadPoolExecutor threadPoolExecutor;
    private static int NUM_THREADS = 10;

    private ThreadPool() {
//        this.threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(NUM_THREADS);
        this.threadPoolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    }
    public static ThreadPool getInstance(){
        if (instance == null){
            instance = new ThreadPool();
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

