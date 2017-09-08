/**
 * Exercise 2.1, question 3
 */

import java.util.concurrent.atomic.AtomicInteger;

class SplitCounter extends Thread {
    private AtomicInteger count;
    private int start, stop;
    
    public SplitCounter(AtomicInteger count, int start, int stop) {
        this.count = count;
        this.start = start;
        this.stop = stop;
    }

    public void run() {
        for (int k = start; k < stop; ++k) {
            count.addAndGet(FactorsCounter.countFactors(k));
        }
    }
}

public class ParallelFactorsCounterWithUtil {
    public static void main(String[] args) {
        long upperBound = 5_000_000;
        int threadsCount = 10;
        int threadSplitLength = 500_000;

        Thread[] threads = new Thread[threadsCount];
        final AtomicInteger count = new AtomicInteger();

        for (int k = 0; k < threadsCount; ++k) {
            threads[k] = new SplitCounter(count,
                k * threadSplitLength, (k + 1) * threadSplitLength);
        }

        for (Thread worker : threads) {
            worker.start();
        }

        try {
            for (Thread worker : threads) {
                worker.join();
            }
        }
        catch (InterruptedException e) {
            System.err.println("A worker was interrup[ted... ");
            e.printStackTrace();
        }

        System.out.println("Total number of factors: " + count.get());
    }
}
