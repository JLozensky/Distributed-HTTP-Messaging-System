package SharedClientClasses;

import java.util.concurrent.CountDownLatch;

public class Gates {
    private CountDownLatch waitGate;
    private CountDownLatch countGate;
    private CountDownLatch finalGate;

    public Gates(CountDownLatch waitGate, CountDownLatch countGate, CountDownLatch finalGate) {
        this.waitGate = waitGate;
        this.countGate = countGate;
        this.finalGate = finalGate;
    }

    public void waitToStart() {
        if (this.waitGate != null) {
            try {
                this.waitGate.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void finishedCountdown() {

        if (this.countGate != null) {
            this.countGate.countDown();
        }

        this.finalGate.countDown();
    }





}
