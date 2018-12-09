import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;

// For the Multiverse library:
import org.multiverse.api.references.*;
import static org.multiverse.api.StmUtils.*;

public class TestCasHistogram {
  public static void main(String[] args) {
    Histogram c = new CasHistogram(30);
    countPrimeFactorsWithStmHistogram(c);

    Histogram t = new StmHistogram(30);
    countPrimeFactorsWithStmHistogram(t);

    Histogram s = new SimpleHistogram(30);
    countPrimeFactorsWithStmHistogram(s);
  }

  private static void countPrimeFactorsWithStmHistogram(Histogram histogram) {
    // final Histogram histogram = new CasHistogram(30);
    // final Histogram total = new CasHistogram(30);
    final int range = 4_000_000;
    final int threadCount = 10, perThread = range / threadCount;
    final CyclicBarrier startBarrier = new CyclicBarrier(threadCount + 1),
      stopBarrier = startBarrier;
    final Thread[] threads = new Thread[threadCount];

    for (int t=0; t<threadCount; t++) {
      final int from = perThread * t,
                  to = (t+1 == threadCount) ? range : perThread * (t+1);
      // System.out.printf("thread_id %d: %d - %d\n", t, from, to);
        threads[t] =
          new Thread(() -> {
        // wait for all threads to be ready
	      try { startBarrier.await(); } catch (Exception exn) { }
        // each thread will do his junk
	      for (int p=from; p<to; p++)
		      histogram.increment(countFactors(p));
	      // System.out.print("*");
        // wait for all threads to be finished
	      try { stopBarrier.await(); } catch (Exception exn) { }
	    });
        // start all threads
        threads[t].start();
    }
    try { startBarrier.await(); } catch (Exception exn) { }

    Timer start = new Timer();

    // test transfer bins
    // for (int i = 0; i < 200; i++) {
    //   total.transferBins(total);
    //   try{ Thread.sleep(30); } catch(InterruptedException exn){}
    // }

    try { stopBarrier.await(); } catch (Exception exn) { }

    double stop = start.check();
    System.out.println(histogram.getClass() + " --> " + stop);

    // dump(histogram);
    // dump(total);
  }

  public static void dump(Histogram histogram) {
    int totalCount = 0;
    for (int bin=0; bin<histogram.getSpan(); bin++) {
      System.out.printf("%4d: %9d%n", bin, histogram.getCount(bin));
      totalCount += histogram.getCount(bin);
    }
    System.out.printf("      %9d%n", totalCount);
  }

  public static int countFactors(int p) {
    if (p < 2)
      return 0;
    int factorCount = 1, k = 2;
    while (p >= k * k) {
      if (p % k == 0) {
        factorCount++;
        p /= k;
      } else
        k++;
    }
    return factorCount;
  }
}

interface Histogram {
  void increment(int bin);
  int getCount(int bin);
  int getSpan();
  int[] getBins();
  int getAndClear(int bin);
  void transferBins(Histogram hist);
}

class CasHistogram implements Histogram {
  private final AtomicInteger[] counts;

  public CasHistogram(int span) {
    counts = new AtomicInteger[span];
    for (int i = 0; i < counts.length; i++) {
      counts[i] = new AtomicInteger(0);
    }
  }

  public void increment(int bin) {
    int oldValue;
    do {
      oldValue = counts[bin].get();
    } while(!counts[bin].compareAndSet(oldValue, oldValue + 1));
  }

  public int getCount(int bin) {
    return counts[bin].get();
  }

  public int getSpan() {
    return counts.length;
  }

  public int[] getBins() {
    return IntStream.range(0, getSpan()).map((bin) -> getCount(bin)).toArray();
  }

  public int getAndClear(int bin) {
    int oldValue;
    do {
      oldValue = counts[bin].get();
    } while(!counts[bin].compareAndSet(oldValue, 0));
    return oldValue;
  }

  public void transferBins(Histogram hist) {
    for (int i = 0; i < getSpan(); i++) {
      final int index = i;
      int oldValue, newValue;
      int delta = hist.getAndClear(index);
      do {
        oldValue = this.getCount(index);
        newValue = delta + oldValue;
      } while(!counts[index].compareAndSet(oldValue, newValue));
    }
  }
}

class StmHistogram implements Histogram {
  private final TxnInteger[] counts;

  public StmHistogram(int span) {
    counts = new TxnInteger[span];
    for (int i = 0; i < counts.length; i++) {
      counts[i] = newTxnInteger(0);
    }
  }

  public void increment(int bin) {
    atomic(() -> counts[bin].increment());
  }

  public int getCount(int bin) {
    return atomic(() -> counts[bin].get());
  }

  public int getSpan() {
    return counts.length;
  }

  public int[] getBins() {
    return IntStream.range(0, getSpan()).map((bin) -> getCount(bin)).toArray();
  }

  public int getAndClear(int bin) {
    return atomic(() ->  counts[bin].getAndSet(0) );
  }

  public void transferBins(Histogram hist) {
    for (int i = 0; i < getSpan(); i++) {
      final int index = i;
      atomic(() -> counts[index].increment(hist.getAndClear(index)));
    }
  }
}

class SimpleHistogram implements Histogram {
  private final int[] counts;
  public SimpleHistogram(int span) {
    this.counts = new int[span];
  }
  public synchronized void increment(int bin) {
    counts[bin] = counts[bin] + 1;
  }
  public synchronized int getCount(int bin) {
    return counts[bin];
  }
  public int getSpan() {
    return counts.length;
  }

  public int[] getBins() {
    throw new RuntimeException("Not implemented");
  }

  public int getAndClear(int bin) {
    throw new RuntimeException("Not implemented");
  }

  public void transferBins(Histogram hist) {
    throw new RuntimeException("Not implemented");
  }
}

class Timer {
    private long start, spent = 0;
    public Timer() { }
    public double check() { return (System.nanoTime()-start+spent)/1e9; }
    public void pause() { spent += System.nanoTime()-start; }
    public void play() { start = System.nanoTime(); }
}
