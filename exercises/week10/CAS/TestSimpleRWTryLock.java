import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.Optional;

// parallelTest
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.Collection;
import java.util.Random;
import java.util.Iterator;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.stream.IntStream;


class TestSimpleRWTryLock {
  public static final int OPERATIONS_PER_THREAD = 100;
  public static void main(String[] args) {
    seqTests();
    parallelTest();
  }

  static void seqTests() {
    System.out.println("Sequential test");
    SimpleRWTryLock casLock = new SimpleRWTryLock();

    // A thread cannot unlock a lock when it has not acquired one.
    try {
      casLock.readerUnlock();
      throw new AssertionError("A thread cannot unluck a lock when it has not acquired one");
    } catch (Exception ex) {}

    // A thread must be able to acquire a lock when it has not acquired one.
    assert casLock.readerTryLock();

    // A thread cannot acquire a write lock when there are active reader locks.
    assert !casLock.writerTryLock();

    // A thread cannot acquire a reader lock twice
    try {
      casLock.readerTryLock();
      throw new AssertionError("A thread must not be able to obtain a reader lock twice");
    }
    catch(Exception ex) {}

    // A thread must be able to unlock its acquired lock
    casLock.readerUnlock();

    // A thread must be able to acquire a write lock when no locks are active
    assert casLock.writerTryLock();

    // A thread must not be able to acquire a reader lock when a write lock is active.
    assert !casLock.readerTryLock();

    // A thread must be able to unlock its own acquired write lock.
    casLock.writerUnlock();

    // A thread must be able to obtain a reader lock after unlocking a write lock.
    assert casLock.readerTryLock();
  }

  public static void parallelTest() {
      System.out.printf("Parallel test");
      final SimpleRWTryLock casLock = new SimpleRWTryLock();


      final int threadsCount = Runtime.getRuntime().availableProcessors() * 4;
      ExecutorService executor = Executors.newWorkStealingPool(threadsCount);

      Collection<Future<Boolean>> futures = new ArrayList<>(threadsCount);
      CyclicBarrier barrier = new CyclicBarrier(threadsCount);
      for (int k = 0; k < threadsCount; ++k) {
      final int index = k;
          futures.add(executor.submit(() -> {
              String value = Thread.currentThread().getName();
              long seed = System.currentTimeMillis() + value.hashCode();
              IntStream randomInts = new Random(seed).ints(OPERATIONS_PER_THREAD);
              barrier.await();
              Iterator<Integer> iterator = randomInts.iterator();
              boolean iHaveReadLock = false;
              boolean iHaveWriteLock = false;

              while (iterator.hasNext()) {
                  int key = iterator.next();

                  //A thread can only have a writer or a reader lock.
                  assert (!(iHaveReadLock && iHaveWriteLock));

                  switch (key % 4) {
                      case 0: {
                        if (iHaveReadLock) {
                          try {
                            casLock.readerTryLock();
                            throw new AssertionError("A thread cannot reader lock when it already have acquired a reader lock");
                          } catch (Exception ex) {}
                        } else {
                          iHaveReadLock = casLock.readerTryLock();
                        }
                        break;
                      }

                      case 1: {
                        if (iHaveWriteLock) {
                          try {
                            casLock.writerTryLock();
                            throw new AssertionError("A thread cannot writer lock when it already have acquired a writer lock");
                          } catch (Exception ex) {}
                        } else {
                          iHaveWriteLock = casLock.writerTryLock();
                        }
                        break;
                      }

                      case 2: {
                        if (iHaveReadLock) {
                          casLock.readerUnlock();
                          iHaveReadLock = false;
                        } else {
                          try {
                            casLock.readerUnlock();
                            throw new AssertionError("A thread cannot unlock when it does not have a lock");
                          } catch (Exception ex) {}
                        }
                        break;
                      }

                      case 3: {
                        if (iHaveWriteLock) {
                          casLock.writerUnlock();
                          iHaveWriteLock = false;
                        } else {
                          try {
                            casLock.writerUnlock();
                            throw new AssertionError("A thread cannot unlock when it does not have a lock");
                          } catch (Exception ex) {}
                        }
                        break;
                      }
                  }
              }
              return true;
          }));
      }

      for (Future<Boolean> fut : futures) {
          try {
                fut.get();
          }
          catch (Exception e) {
              StringWriter writer = new StringWriter();
              PrintWriter printWriter = new PrintWriter(writer);
              e.printStackTrace(printWriter);
              String stackTrace = writer.toString();
              System.out.println(stackTrace);
          }
      }
  }
}

interface ISimpleRWTryLock {
  boolean readerTryLock();
  void readerUnlock();
  boolean writerTryLock();
  void writerUnlock();
}

abstract class Holders {
  public final Thread thread;
  public Holders(Thread t) {
    thread = t;
  }
}

/*
  The ReaderList class is used to represent an immutable
  linked list of the threads that hold read locks
*/

class ReaderList extends Holders {
  private final ReaderList next;
  public ReaderList(Thread t, ReaderList next) {
    super(t);
    this.next = next;
  }

  /*
      Returns a boolean whether the current or
      children ReaderList has a given thread.
  */
  public boolean contains(Thread t) {
    if (thread == t) return true;
    if (next == null) return false;
    return next.contains(t);
  }

  /*
    Returns a new ReaderList with the ReaderList
    containing given thread t removed.
  */
  public ReaderList remove(Thread t) {
    if (thread == t)
      return next;
    return new ReaderList(thread, Optional.ofNullable(next).map(x->x.remove(t)).orElse(null));
  }
}

/*
  The Writer class is used to represent
  a thread that holds the write lock.
*/

class Writer extends Holders {
  public Writer(Thread t) {
    super(t);
  }
}

/*
    SimpleRWTryLock that is not reentrant
    and that does not block.
*/

class SimpleRWTryLock implements ISimpleRWTryLock {
  private final AtomicReference<Holders> holder = new AtomicReference<Holders>();

  // acquire a writelock for current thread
  public boolean writerTryLock() {
    final Thread current = Thread.currentThread();
    // already held by the current thread
    if ((holder.get() instanceof Writer) && (holder.get().thread == current)) {
      throw new IllegalStateException();
    }
    else if (holder.compareAndSet(null, new Writer(current))) return true;
    return false;
  }

  public void writerUnlock() {
    final Thread current = Thread.currentThread();
    if ((holder.get() instanceof Writer) && (holder.get().thread == current)) {
      holder.set(null);
    } else {
      throw new IllegalStateException();
    }
  };

  public boolean readerTryLock() {
    Holders holder = this.holder.get();
    final Thread current = Thread.currentThread();

    // A reader lock cannot be acquired if a writer lock is active.
    if (holder instanceof Writer) {
      return false;
    }

    // A reader thread cannot acquire multiple reader locks.
    if (holder != null && ((ReaderList)holder).contains(current)) {
      throw new IllegalStateException();
    }


    while(!this.holder.compareAndSet(holder, new ReaderList(current, (ReaderList)holder))) {
      holder = this.holder.get();
      if (holder instanceof Writer) return false;
    }
    return true;
  }

  /*
	   Unlock a read lock acquired for current thread.
	*/
  public void readerUnlock() {
    Holders rootHolder = null;
    Holders newHolder = null;
    final Thread current = Thread.currentThread();

    do {
      Holders holder = this.holder.get();
      newHolder = null;
      rootHolder = holder;
      // A reader lock can only exist if the holder is a ReaderList instance.
      if (holder instanceof Writer) {
        throw new IllegalStateException();
      }

      // A reader lock can only be unlocked if it exists.
      if (!((ReaderList)holder).contains(current)) {
        throw new IllegalStateException();
      }

      // get a new ReaderList without the reader lock of current Thread
      newHolder = ((ReaderList)holder).remove(current);

      // Attempt to update ReaderList holder.
    } while (!holder.compareAndSet(rootHolder, newHolder));
  }

}
