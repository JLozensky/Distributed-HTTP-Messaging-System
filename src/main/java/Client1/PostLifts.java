package Client1;

import java.util.concurrent.CountDownLatch;

public class PostLifts implements Runnable{
    private int skierStart;
    private int skierEnd;
    private int startTime;
    private int endTime;
    private int numPosts;

    public PostLifts(int skierStart, int skierEnd, int startTime, int endTime, int numPosts, CountDownLatch countGate,
        CountDownLatch waitGate, CountDownLatch finalGate){}



    /**
     * When an object implementing interface <code>Runnable</code> is used to create a thread, starting the thread
     * causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {

    }
}
