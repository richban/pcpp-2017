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

Also I have created a *parallelTest* with N-threads with aggregate results.
For this test I have used the **CyclicBarrier(N)** and used the Consumer & Producer
pattern. First I create *npairs* of Producers and Consumers. The Producers
pushes *nTrials* random numbers and the consumer pops *nTrials* from the stack.
Afterwards I check the consumed numbers is equals to the sum of the produced
numbers. Both of them are summing a thread-local *sum* variable and than adding
the result to a common  AtomicInteger. Thus the implementation meets it's functional
requirement in a concurrent setting.

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
STACK                 1     85242089900.0 ms 20000051903.20          2
STACK                 2    154794872250.0 ms 18465084607.51          2
STACK                 3    110640898200.0 ms 12998640618.15          2
STACK                 4    149573320300.0 ms 16993331468.59          2
STACK                 5    147559105350.0 ms 7478417483.04          2
STACK                 6    148149278750.0 ms 9311182073.98          2
STACK                 7    145702732500.0 ms 4666769875.10          2
STACK                 8    143224915450.0 ms 4515393894.18          2
STACK                 9    142721823100.0 ms 3400317717.23          2
STACK                 10   145412906200.0 ms 3413457925.33          2
STACK                 11   143447644200.0 ms 3097260090.47          2
STACK                 12   141824955300.0 ms 4945770882.33          2
STACK                 13   142242470000.0 ms 3174866220.53          2
STACK                 14   143949764500.0 ms 2786600769.20          2
STACK                 15   146404587400.0 ms 16108677239.63          2
STACK                 16   141004786100.0 ms 3715933378.64          2
STACK                 17   145776930850.0 ms 5425092691.89          2
STACK                 18   140945899950.0 ms 3102080430.00          2
STACK                 19   144937240000.0 ms 3903865458.53          2
STACK                 20   142560768200.0 ms 4152427160.12          2
STACK                 21   145882030400.0 ms 13628822709.99          2
STACK                 22   140082925600.0 ms 6151480125.63          2
STACK                 23   139846953100.0 ms 4538194164.35          2
STACK                 24   135710651700.0 ms 3899262540.33          2
STACK                 25   138939403300.0 ms 3091765027.61          2
STACK                 26   141089838050.0 ms 13244149579.92          2
STACK                 27   142873807000.0 ms 12930054559.33          2
STACK                 28   140328849300.0 ms 13021087756.13          2
STACK                 29   140079733950.0 ms 18018942211.23          2
STACK                 30   141744063750.0 ms 11727383858.44          2
STACK                 31   138717250350.0 ms 10508410403.80          2
STACK                 32   136048098000.0 ms 4411981894.80          2
STRIPED-STACK         1     98347358400.0 ms 6605904659.83          2
STRIPED-STACK         2     63409075100.0 ms 19217268733.21          2
STRIPED-STACK         3     46819600700.0 ms 12418486713.10          2
STRIPED-STACK         4     55773598800.0 ms 23092743258.50          2
STRIPED-STACK         5     40165670550.0 ms 18609587553.72          2
STRIPED-STACK         6     44288681100.0 ms 13993533557.24          2
STRIPED-STACK         7     37984089100.0 ms 10400180101.95          2
STRIPED-STACK         8     39983661250.0 ms 6394667765.71          2
STRIPED-STACK         9     38248943400.0 ms 9359360410.59          2
STRIPED-STACK         10    34425366000.0 ms 7256933325.20          2
STRIPED-STACK         11    28523888100.0 ms 5233178207.45          2
STRIPED-STACK         12    32454405050.0 ms 7301581872.60          2
STRIPED-STACK         13    28463636650.0 ms 2699522715.31          2
STRIPED-STACK         14    29759544950.0 ms 6042382988.13          2
STRIPED-STACK         15    29613413950.0 ms 5673564993.67          2
STRIPED-STACK         16    30148057300.0 ms 3815856696.59          2
STRIPED-STACK         17    28919513950.0 ms 4088385621.80          2
STRIPED-STACK         18    29649181250.0 ms 1789205552.25          2
STRIPED-STACK         19    29588034200.0 ms 3047020738.90          2
STRIPED-STACK         20    31762340750.0 ms 7682270558.81          2
STRIPED-STACK         21    29399609300.0 ms 5106695422.61          2
STRIPED-STACK         22    29326752950.0 ms 3759901953.09          2
STRIPED-STACK         23    28968637600.0 ms 2696811799.53          2
STRIPED-STACK         24    29030478750.0 ms 3629115586.94          2
STRIPED-STACK         25    29312783400.0 ms 3070344498.46          2
STRIPED-STACK         26    28780310600.0 ms 4190776809.73          2
STRIPED-STACK         27    28038794400.0 ms 2943390587.04          2
STRIPED-STACK         28    29068804900.0 ms 3607733292.01          2
STRIPED-STACK         29    29968892450.0 ms 3996147030.70          2
STRIPED-STACK         30    27554833950.0 ms 1987679844.10          2
STRIPED-STACK         31    27844678350.0 ms 3963508507.28          2
STRIPED-STACK         32    28194843900.0 ms 3069944051.15          2
```

*We want to improve the performance by lock striping, using the following ideas*

**Implementation: ConcurrentStack.java -> class StripedStack {}**

*Run a performance test. Do you see the hoped for improvement in performance*

Each test represents the same amount of work.

Synchronized stack implementation scales poorly on my multicore machine, because
of the locking mechanism - only one thread at a time can perform ```push()``` or ```pop()```.
at the same time. The entire stack is guarded by a single lock. However synchronization
shows some improvement up to 3 threads, but than goes up as synchronization overhead increases.
By the time it hits 5 threads, contention is so heavy that every access to the stack lock
is contended.

 So long as contention  is low, time  per operation is dominated by the time to actually do the work and throughput may improve as threads are added. Once contention becomes significant, time per  operation is dominated by context switch and scheduling delays, and adding more threads has  little  effect on throughput.

Striped implementation appears to be almost 5 times faster than synchronized stack.
This can be interpreted as a confirmation that lock contention is a great source of
sequentiality in parallel computations. Having 32 stripes (having a lock for every bucket)
improve performance, however stripes are not related in any way to the number of threads.
The lock used when accessing the data structure depends on the thread alone. A higher number of stripes means less chances of two or more threads trying to acquire the same lock.


![alt text](../Results/linkedlist.png "Performance: synchronized stack vs striped stack")
