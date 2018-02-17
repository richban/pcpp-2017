# Question 4

### 4.1
* Why is this implementation dead-lock prone?

The implementation is dead-lock prone because of the lack of consistent lock coordination.
In fact a thread acquires two locks but it does not pick the locations of the elements in a deterministic order. Therefore it might happen that 2 threads trying to acquire the same locks but in opposite directions making them block each other.

### 4.2
* Give an example program using this data structure and a schedule that deadlocks.

### 4.3
* Implement a test program that uses the above data structure and provokes a deadlock with reasonable probability and run it. Do you see it deadlocking? Discuss your findings.

```
  public void deadlockTest(int x, int y, final UnionFind uf) throws Exception {
    System.out.printf("Testing %s ... ", uf.getClass());
    final int threadCount = 32;
    final CyclicBarrier startBarrier = new CyclicBarrier(threadCount*2),
      stopBarrier = startBarrier;
      for (int i = 0; i < threadCount; ++i) {
        Thread ti = new Thread(new Runnable() { public void run() {
          try { startBarrier.await(); } catch (Exception exn) { }
            uf.union(x, y);
          try { stopBarrier.await(); } catch (Exception exn) { }
        }});
        Thread tj = new Thread(new Runnable() { public void run() {
          try { startBarrier.await(); } catch (Exception exn) { }
            uf.union(y, x);
          try { stopBarrier.await(); } catch (Exception exn) { }
        }});
        ti.start();
        tj.start();
      }
      startBarrier.await();
      stopBarrier.await();
      System.out.println("passed");
  }
}
```

In order for scheduling to happen as I have described before I run many threads sequaniatly and tested that on my machine I had to increase the number of threads to deadlock...

*Results*

```
Testing class CoarseUnionFind ... passed
Testing class FineUnionFind ... passed
Testing class WaitFreeUnionFind ... passed
Testing class BogusFineUnionFind ... passed
```

```
Testing class CoarseUnionFind ... passed
Testing class FineUnionFind ... passed
Testing class WaitFreeUnionFind ... passed
Testing class BogusFineUnionFind ...
```
