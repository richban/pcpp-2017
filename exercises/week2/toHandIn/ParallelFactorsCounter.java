/**
 * Exercise 2.1, question 3
 */

package toHandin;

public class ParallelFactorsCounter {
    public static void main(String[] args) {
        long upperBound = 5_000_000;
        int threadsCount = 10;
        int threadSplitLength = 500_000;

        Thread[] threads = new Thread[threadsCount];
        final MyAtomicInteger count = new MyAtomicInteger();

        for (int k = 0; k < threadsCount; ++k) {
            final int start = k * threadSplitLength;
            final int stop = (k + 1) * threadSplitLength;

            threads[k] = new Thread(() -> {
                for (int h = start; h < stop; ++h) {
                    count.addAndGet(FactorsCounter.countFactors(h));
                }
            });
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
