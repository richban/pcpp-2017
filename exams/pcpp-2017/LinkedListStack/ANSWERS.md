# Question 5

### LinkedListStack:

*Implementation:* ConcurrentStack.java

*Implement the data structure as a classical linked list (e.g. using Java’s LinkedList type) with a single global lock.*



*Correctness: are the three functional requirements above met in a concurrent setting?*

For the correctness of the functional requirements I have created a very
basic single-threaded functional testing while implementing the stack based
linked list, the method name is called *seqTest*.

```
private static void seqTest(final ConcurrentStackImp stack) {
  System.out.printf("Sequential test %n%s%n", stack.getClass());

  assert stack.size() == 0;
  stack.push(1);
  stack.push(2);
  assert stack.size() == 2;
  assert stack.pop() == 2;
  assert stack.pop() == 1;
  assert stack.pop() == null;
}
```

Also I have creating a *parallelTest* with N-threads with aggregate results.
For this test I have used the **CyclicBarrier(N)** and used the Consumer & Producer
pattern. First I create *npairs* of Producers and Consumers. The Producers
pushes *nTrials* random numbers and the consumer pops *nTrials* from the stack.
Afterwards I check the consumed numbers is equals to the sum of the produced
numbers. Both of them are summing a thread-local *sum* variable and than adding
the result to a common  AtomicInteger.

```
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
```
