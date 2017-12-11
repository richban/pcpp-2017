import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentStack {
  public static void main(String[] args) {
    System.out.printf("STACK");
    seqTest(new ConcurrentStackImp<Integer>());
    parallelTest(new ConcurrentStackImp<Integer>());
  }

  private static void parallelTest(final ConcurrentStackImp<Integer> stack) {
    System.out.printf("%nParallel test: %s", stack.getClass());
    final ExecutorService pool = Executors.newCachedThreadPool();
    new PushPopTest(stack, 10, 10).test(pool);
    pool.shutdown();
    System.out.println("... passed");
  }

  private static void seqTest(final ConcurrentStackImp<Integer> stack) {
    System.out.printf("Sequential test %n%s%n", stack.getClass());

    assert stack.size() == 0;
    stack.push(1);
    stack.push(2);
    assert stack.size() == 2;
    assert stack.pop() == 2;
    assert stack.pop() == 1;
    assert stack.pop() == null;

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
  protected final ConcurrentStackImp<Integer> stack;
  protected final int nTrials, nPairs;
  protected final AtomicInteger putSum = new AtomicInteger(0);
  protected final AtomicInteger takeSum = new AtomicInteger(0);

  public PushPopTest(ConcurrentStackImp<Integer> stack, int npairs, int ntrials) {
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
          System.out.printf("%nSize Produced: %s", stack.size());
        }
        putSum.getAndAdd(sum);
        stopBarrier.await();
        System.out.printf("%nSize Produced: %s", stack.size());
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  class Consumer implements Runnable {
    public void run() {
      try {
        System.out.printf("%nSize to Consume: %s", stack.size());
        startBarrier.await();
        int sum = 0;
        for (int i = nTrials; i > 0; --i) {
          sum += stack.pop();
          System.out.printf("%nSize Consumed: %s", stack.size());
        }
        takeSum.getAndAdd(sum);
        stopBarrier.await();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
}

interface ConcurrentStackList<E> {
  void push(E e);
  E pop();
  int size();
}

class ConcurrentStackImp<E> implements ConcurrentStackList<E> {
  private LinkedList<E> stack = new LinkedList<E>();
  private final Object lock = new Object();

  public void push(E e) {
    synchronized (lock) {
      stack.push(e);
    }
  }

  public E pop() {
    synchronized (lock) {
      if (!(stack.isEmpty())) {
        return stack.pop();
      }
      else return null;
    }
  }

  public int size() {
    return stack.size();
  }

  public boolean isEmpty() {
    return stack.isEmpty();
  }
}
