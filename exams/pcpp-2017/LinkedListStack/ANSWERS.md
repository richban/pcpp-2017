# Question 5

### LinkedListStack:

```
# OS:   Mac OS X; 10.13.3; x86_64
# JVM:  Oracle Corporation; 1.8.0_144
# CPU:  null; 8 "cores"
# Date: 2018-02-19T21:27:31+0100
```

*Implement the data structure as a classical linked list (e.g. using Java’s LinkedList type) with a single global lock.*

**Implementation:** ConcurrentStack.java

The implementation follows the Java monitor pattern: all mutable fields are private,
all public methods are synchronized, and no internal data structures escapes.

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

*Performance*

```
STACK                 1   1154046900000.0 us 638852060767.93          2
STACK                 2   1440819100000.0 us 345149951903.90          2
STACK                 3    808908300000.0 us 36570813608.82           2
STACK                 4    923729350000.0 us 225287299566.46          2
STACK                 5    979386900000.0 us 297001563508.68          2
STACK                 6    912873900000.0 us 158227101662.07          2
STACK                 7    858515050000.0 us 153598975339.00          2
STACK                 8    820701600000.0 us 370105177957.55          2
STACK                 9    881084950000.0 us 133297981692.74          2
STACK                 10   922332700000.0 us 183554819872.28          2
STACK                 11  1691643100000.0 us 285135043876.27          2
STACK                 12   844014300000.0 us 162409074457.95          2
STACK                 13   856872000000.0 us 348818537776.85          2
STACK                 14   840118150000.0 us 370701332681.79          2
STACK                 15   886889600000.0 us 160609349088.75          2
STACK                 16   914552600000.0 us 350710412496.45          2
STACK                 17   885067350000.0 us 163807689060.65          2
STACK                 18   964158600000.0 us 144789777210.66          2
STACK                 19  1097849750000.0 us 259243322886.72          2
STACK                 20   997320100000.0 us 182125436569.87          2
STACK                 21  1054127400000.0 us 208950148020.18          2
STACK                 22  1269539600000.0 us 245572585578.84          2
STACK                 23  1280002100000.0 us 267614292284.60          2
STACK                 24  1235818150000.0 us 313159192455.96          2
STACK                 25  1383544350000.0 us 330531062976.44          2
STACK                 26  1500327450000.0 us 250018941816.13          2
STACK                 27  1426553550000.0 us 259651751803.84          2
STACK                 28  1641479100000.0 us 371752434650.46          2
STACK                 29  1552492350000.0 us 391481097157.89          2
STACK                 30  1531795050000.0 us 221013091002.23          2
STACK                 31  1808614900000.0 us 215289526893.41          2
STACK                 32  1675416400000.0 us 241262993886.78          2
```
