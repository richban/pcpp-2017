# Question 4

### 4.1
* Why is this implementation dead-lock prone?

The implementation is dead-lock prone because of the lack of consistent lock coordination.
In fact a thread acquires two locks but it does not pick the locations of the elements in a deterministic order. Therefore it might happen that 2 threads trying to acquire the same locks but in opposite directions making them block each other.

### 4.2
* Give an example program using this data structure and a schedule that deadlocks.

Thread A comes and acquires the lock at index 7 and between acquiring the second lock at index 10 B thread is scheduled and acquires the lock at index 10, therefore 10 is locked and thread A cannot progress, and either B because it's trying to acquire the lock at index 7 that is being hold by thread A. Thus they're trying to acquiring the same lock and are in dedlock now.

```
synchronized (nodes[rx]) {
    // A thread acquires lock on 7 and wants to acquire the lock on 10
    // however between the synchronized methods B threads comes in and
    // acquires the lock on 10 and wants to acquire the lock on 7 which
    // is beaing hold by thread A.
    synchronized (nodes[ry]) {
```
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

In order for deadlock scheduling to happen as I have described in the previous question I have run many threads sequentially and tested that on my machine I had to increase the number of threads to ensure the bigger likelihood of deadlocking.

*Results*

Results with 8 threads.

```
Testing class CoarseUnionFind ... passed
Testing class FineUnionFind ... passed
Testing class WaitFreeUnionFind ... passed
Testing class BogusFineUnionFind ... passed
```
Results with 16 threads.
```
Testing class CoarseUnionFind ... passed
Testing class FineUnionFind ... passed
Testing class WaitFreeUnionFind ... passed
Testing class BogusFineUnionFind ... deadlock
```
