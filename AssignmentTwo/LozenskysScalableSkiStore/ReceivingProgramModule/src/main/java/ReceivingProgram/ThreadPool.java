package ReceivingProgram;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPool {
    private static ThreadPool instance = null;

        private ThreadPoolExecutor threadPoolExecutor;
        private static int NUM_THREADS = 50;

        private ThreadPool() {

        this.threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(NUM_THREADS);

                }
        public static ThreadPool getInstance(){
                if (instance == null){
                    instance = new ThreadPool();
                }
                return instance;
            }

        public void runThread(Runnable runnable) {
            this.threadPoolExecutor.execute(runnable);
        }
}
