import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntToDoubleFunction;


public class ConcurrentStack {
  public static void main(String[] args) {
    SystemInfo();
    System.out.printf("LINKEDLIST STACK TEST IMPLEMENTATION\n");
    // // Sequential test for correctness of the implemantation
    // seqTest(new ConcurrentStackImp());
    // // Parallel test for the correctness of the implemantation
    // parallelTest(new ConcurrentStackImp());
    // // Benchmark performance test using Mark7
    timeAllMaps();
    // seqStripedTest(new StripedStack(32, 32));
  }

  private static void timeAllMaps() {
    for (int t=1; t<=32; t++) {
      final int threadCount = t;
      Mark7(String.format("%-21s %d", "STACK", threadCount),
            i -> timeMap(threadCount, new ConcurrentStackImp()));
    }
    for (int t=1; t<=32; t++) {
      final int threadCount = t;
      Mark7(String.format("%-21s %d", "STRIPED STACK", threadCount),
            i -> timeMap(threadCount, new StripedStack(32, 32)));
      }
  }

  private static double timeMap(int threadCount, final ConcurrentStackList stack) {
    final int iterations = 1_000_000, perThread = iterations / threadCount;
    final int range = 200_000;
    return exerciseMap(threadCount, perThread, range, stack);
  }

  // TO BE HANDED OUT
private static double exerciseMap(int threadCount, int perThread, int range,
                                  final ConcurrentStackList stack) {
  Thread[] threads = new Thread[threadCount];
  for (int t=0; t<threadCount; t++) {
    final int myThread = t;
    threads[t] = new Thread(() -> {
      Random random = new Random(37 * myThread + 78);
      for (int i=0; i<perThread; i++) {
        Integer item = random.nextInt(range);
        // Add item with probability 60%
        if (random.nextDouble() < 0.60) {
          stack.push(item);
        }
        else // pop item with probability 20% and reinsert
          if (random.nextDouble() < 0.20) {
            stack.pop();
          }
      }
    });
  }
  for (int t=0; t<threadCount; t++)
    threads[t].start();
  try {
    for (int t=0; t<threadCount; t++)
      threads[t].join();
  } catch (InterruptedException exn) { }
  // all the other threads stop
  return stack.size();
}

  private static void parallelTest(final ConcurrentStackImp stack) {
    int trials = 10_000;
    int npairs = 10;
    int threadCount = npairs * 2 + 1;
    System.out.printf("%nParallel test: %s \n", stack.getClass());
    final ExecutorService pool = Executors.newCachedThreadPool();
    new PushPopTest(stack, npairs, trials).test(pool);
    pool.shutdown();
  }

  private static void seqTest(final ConcurrentStackImp stack) {
    System.out.printf("Sequential test %s", stack.getClass());

    assert stack.size() == 0;
    stack.push(1);
    stack.push(2);
    assert stack.size() == 2;
    assert stack.pop() == 2;
    assert stack.pop() == 1;
    assert stack.pop() == null;
  }

  private static void seqStripedTest(final StripedStack stack) {
    System.out.printf("Sequential test for StipedStac %n%s%n", stack.getClass());
    System.out.println();
    stack.push(10);
  }

  // NB: Modified to show microseconds instead of nanoseconds

public static double Mark7(String msg, IntToDoubleFunction f) {
  int n = 10, count = 1, totalCount = 0;
  double dummy = 0.0, runningTime = 0.0, st = 0.0, sst = 0.0;
  do {
    count *= 2;
    st = sst = 0.0;
    for (int j=0; j<n; j++) {
      Timer t = new Timer();
      for (int i=0; i<count; i++)
        dummy += f.applyAsDouble(i);
      runningTime = t.check();
      double time = runningTime * 1e3 / count; // miliseconds
      st += time;
      sst += time * time;
      totalCount += count;
    }
  } while (runningTime < 0.25 && count < Integer.MAX_VALUE/2);
  double mean = st/n, sdev = Math.sqrt((sst - mean*mean*n)/(n-1));
  System.out.printf("%-25s %15.1f ms %10.2f %10d%n", msg, mean, sdev, count);
  return dummy / totalCount;
}

public static void SystemInfo() {
  System.out.printf("# OS:   %s; %s; %s%n",
                    System.getProperty("os.name"),
                    System.getProperty("os.version"),
                    System.getProperty("os.arch"));
  System.out.printf("# JVM:  %s; %s%n",
                    System.getProperty("java.vendor"),
                    System.getProperty("java.version"));
  // The processor identifier works only on MS Windows:
  System.out.printf("# CPU:  %s; %d \"cores\"%n",
                    System.getenv("PROCESSOR_IDENTIFIER"),
                    Runtime.getRuntime().availableProcessors());
  java.util.Date now = new java.util.Date();
  System.out.printf("# Date: %s%n",
    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(now));
}

}

class Tests {
  public static void assertEquals(int x, int y) throws Exception {
    if (x != y)
      throw new Exception(String.format("ERROR: %d not equal to %d%n", x, y));
  }

  public static void assertTrue(boolean b) throws Exception {
    if (!b)
      throw new Exception(String.format("ERROR: assertTrue"));
  }
}

class PushPopTest extends Tests {
  protected CyclicBarrier startBarrier, stopBarrier;
  protected final ConcurrentStackImp stack;
  protected final int nTrials, nPairs;
  protected final AtomicInteger putSum = new AtomicInteger(0);
  protected final AtomicInteger takeSum = new AtomicInteger(0);

  public PushPopTest(ConcurrentStackImp stack, int npairs, int ntrials) {
    this.stack = stack;
    this.nTrials = ntrials;
    this.nPairs = npairs;
    this.startBarrier = new CyclicBarrier(npairs * 2 + 1);
    this.stopBarrier = new CyclicBarrier(npairs * 2 + 1);
  }

  void test(ExecutorService pool) {
    try {
      for (int i = 0; i < nPairs; i++) {
        pool.execute(new Producer());
        pool.execute(new Consumer());
      }
      startBarrier.await(); // wait for all threads to be ready
      stopBarrier.await();  // wait for all threads to finish
      assertTrue(stack.isEmpty());
      assertEquals(putSum.get(), takeSum.get());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  class Producer implements Runnable {
    public void run() {
      try {
        Random random = new Random();
        int sum = 0;
        startBarrier.await();
        for (int i = nTrials; i > 0; --i) {
          int item = random.nextInt();
          stack.push(item);
          sum += item;
        }
        putSum.getAndAdd(sum);
        stopBarrier.await();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  class Consumer implements Runnable {
    public void run() {
      try {
        startBarrier.await();
        int sum = 0;
        for (int i = nTrials; i > 0; --i) {
          Integer item = null;
          while (item == null) { item = stack.pop(); }
          sum += item;
        }
        takeSum.getAndAdd(sum);
        stopBarrier.await();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
}

interface ConcurrentStackList {
  void push(int e);
  Integer pop();
  int size();
  boolean isEmpty();
}

class ConcurrentStackImp implements ConcurrentStackList {
  private LinkedList<Integer> stack = new LinkedList<Integer>();
  private int cachedSize;

  public synchronized void push(int e) {
    cachedSize++;
    stack.push(e);
  }

  public synchronized Integer pop() {
      if (!(stack.isEmpty())) {
        cachedSize--;
        return stack.pop();
      }
      else return null;
  }

  public synchronized int size() {
    return cachedSize;
  }

  public synchronized boolean isEmpty() {
    return stack.isEmpty();
  }
}

class StripedStack implements ConcurrentStackList {
  ConcurrentStackImp[] buckets;
  private final int lockCount;
  private final Object[] locks;
  private final int[] sizes;

  public StripedStack(int bucketCount, int lockCount) {
    this.buckets = makeBuckets(bucketCount);
    this.lockCount = lockCount;
    this.locks = new Object[lockCount];
    this.sizes = new int[lockCount];
    for (int stripe=0; stripe < buckets.length; stripe++) {
      this.buckets[stripe] = new ConcurrentStackImp();
      this.locks[stripe] = new Object();
    }
  }

  @SuppressWarnings("unchecked")
  private static ConcurrentStackImp[] makeBuckets(int size) {
  // Java's @$#@?!! type system requires this unsafe cast
    return (ConcurrentStackImp[])new ConcurrentStackImp[size];
  }

  // Protect against poor hash functions and make non-negative
  private static int getHash(Thread t) {
    final int th = t.hashCode();
    // System.out.printf("th: %d\n", th);
    return (th ^ (th >>> 16)) & 0x7FFFFFFF;
  }

  public void push(int e) {
    Thread thread = Thread.currentThread();
    final int h = getHash(thread), stripe = h % buckets.length;
    synchronized (locks[stripe]) {
      //System.out.printf("hash: %d\n", stripe);
      buckets[stripe].push(e);
    }

  }

  public Integer pop() {
    Thread thread = Thread.currentThread();
    final int h = getHash(thread), stripe = h % buckets.length;
    Integer item = null;
    synchronized (locks[stripe]) {
      item = buckets[stripe].pop();
    }
    if (item == null) {
      for (int i=0;i < lockCount ; i++) {
        synchronized (locks[i]) {
          item = buckets[i].pop();
          if (item != null) { return item; }
        }
      }
    }
    return null;
  }

  @Override
  public int size() {
    int result = 0;
    for (int stripe = 0; stripe < buckets.length; stripe++) {
      synchronized (locks[stripe]) {
        result += buckets[stripe].size();
      }
    }
    return result;
  }

  @Override
  public boolean isEmpty() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

}

class Timer {
  private long start, spent = 0;
  public Timer() { play(); }
  public double check() { return (System.nanoTime()-start+spent); }
  public void pause() { spent += System.nanoTime()-start; }
  public void play() { start = System.nanoTime(); }
}
