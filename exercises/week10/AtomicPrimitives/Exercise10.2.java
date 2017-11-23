import java.util.concurrent.atomic.AtomicReference;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.IntStream;
import java.util.Random;
import java.io.StringWriter;
import java.io.PrintWriter;

class TestSimpleRWTryLock {
    public static final int OPERATIONS_PER_THREAD = 100;
	public static void main(String[] args) {  
		performSequentialTest();
		performParallelTest();
	}
	
	/** 
		Sequential test of SimpleRWTryLock
	*/
	static void performSequentialTest()
	{
		System.out.println("Sequential test"); 
		SimpleRWTryLock testClass = new SimpleRWTryLock();
		
		//A thread cannot unlock a lock when it has not acquired one.
		try {
			testClass.readerUnlock();
			throw new AssertionError("A thread cannot unluck a lock when it has not acquired one"); 
		}
		catch (Exception ex) { }
		
		//A thread must be able to acquire a lock when it has not acquired one.
		assert testClass.readerTryLock();
		
		//A thread cannot acquire a write lock when there are active reader locks.
		assert !testClass.writerTryLock(); 
		
		//A thread cannot acquire a reader lock twice
		try {
			testClass.readerTryLock();
			throw new AssertionError("A thread must not be able to obtain a reader lock twice"); 
		}
		catch(Exception ex)  { }
		
		//A thread must be able to unlock its acquired lock
		testClass.readerUnlock();
		
		//A thread must be able to acquire a write lock when no locks are active
		assert testClass.writerTryLock();
		
		//A thread must not be able to acquire a reader lock when a write lock is active.
		assert !testClass.readerTryLock(); 
		
		//A thread must be able to unlock its own acquired write lock.
		testClass.writerUnlock(); 
		
		//A thread must be able to obtain a reader lock after unlocking a write lock.
		assert testClass.readerTryLock();
		
		
	}
	
	static void performParallelTest(){
		System.out.println("Parrallel test"); 
		final SimpleRWTryLock testClass = new SimpleRWTryLock(); 
		
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
						if (iHaveReadLock)
							try{
								testClass.readerTryLock(); 
								throw new AssertionError("A thread cannot reader lock when it already have acquired a reader lock"); 
								} catch (Exception ex) { }
						else
						{
							iHaveReadLock = testClass.readerTryLock();							
						} 
                        break;
					  }
                      case 1: {
						if (iHaveWriteLock)
							try{
								testClass.writerTryLock(); 
								throw new AssertionError("A thread cannot writer lock when it already have acquired a writer lock"); 
								} catch (Exception ex) { }
						else
							iHaveWriteLock = testClass.writerTryLock();
                        break;
					  }
                        case 2: {
						if (iHaveReadLock) {
						  testClass.readerUnlock();
						  iHaveReadLock = false;
						}
					    else
							try{
								testClass.readerUnlock(); 
								throw new AssertionError("A thread cannot unlock when it does not have a lock"); 
								} catch (Exception ex) { }
                          break;
						}
                        case 3: {
						 if (iHaveWriteLock) {
						  testClass.writerUnlock();
						  iHaveWriteLock = false;
						 }
					    else
							try{
								testClass.writerUnlock(); 
								throw new AssertionError("A thread cannot unlock when it does not have a lock"); 
								} catch (Exception ex) { }
                          break;
						}
                  }
              } 
			  return true;
          }));
      }
	  
	  
      for (Future<Boolean> fut : futures)
		try {fut.get(); }catch (Exception ex) { 
	StringWriter writer = new StringWriter();
PrintWriter printWriter = new PrintWriter( writer );
ex.printStackTrace( printWriter );
printWriter.flush();

String stackTrace = writer.toString();
System.out.println(stackTrace);
	
	}
	}
	 
}
abstract class Holders { 
   public final Thread thread; 
   public Holders(Thread t) {
	thread = t;
   } 
}
class ReaderList extends Holders { 
	private final ReaderList next;
	public ReaderList(Thread thread, ReaderList next)
	{
		super(thread);
		this.next = next; 
	}
	
	/** 
		Returns a boolean whether the current or children ReaderList 
		has a given thread.
	*/
	public boolean contains(Thread t) { 
		if (thread == t) return true;
		if (next == null) return false;
		return next.contains(t);
	}
	
	/** 
		Returns a new ReaderList with the ReaderList containing given
		thread t removed.
	*/
	public ReaderList remove(Thread t) {
		if (thread == t)
			return next; 
		return new ReaderList(thread,Optional.ofNullable(next).map(x->x.remove(t)).orElse(null));
	} 
}

class Writer extends Holders { 
	public Writer(Thread thread){
		super(thread);
	}
}

/** 
		SimpleRWTryLock read/write class
*/
class SimpleRWTryLock {
	AtomicReference<Holders> holder =  new AtomicReference<Holders>();
	
	/** 
			acquire a read lock for current thread.
	*/
	public boolean readerTryLock() { 
		Holders holder = this.holder.get(); 
		
		//A reader lock cannot be acquired if a writer lock is active.
		if (holder instanceof Writer)
			return false;
		
		//A reader thread cannot acquire multiple reader locks.
		if (holder != null && ((ReaderList)holder).contains(Thread.currentThread()))
				throw new IllegalStateException(); 
		
		//Append reader lock to ReaderList
		while (!this.holder.compareAndSet(holder, new ReaderList(Thread.currentThread(),(ReaderList)holder))) {		 
			holder = this.holder.get();	
			if (holder instanceof Writer)
				return false;
		}
		
		return true;
	}
	
	/** 
			unlock a read lock acquired for current thread.
	*/
	public void readerUnlock() { 
		Holders rootHolder = null;
		Holders newHolder = null;
		do {
			Holders holder = this.holder.get(); 
			newHolder = null;
			rootHolder = holder; 
			
			//A reader lock can only exist if the holder is a ReaderList instance.
			if (!(holder instanceof ReaderList))
				throw new IllegalStateException();
					
			//A reader lock can only be unlocked if it exists.
			if (!((ReaderList)holder).contains(Thread.currentThread())) 
				throw new IllegalStateException();
			
			//get a new ReaderList without the reader lock of current Thread
			newHolder = ((ReaderList)holder).remove(Thread.currentThread());	

		//Attempt to update ReaderList holder.
		} while (!holder.compareAndSet(rootHolder,newHolder));
			 
	}	
	
	/** 
			acquire a write lock for current thread.
	*/
	public boolean writerTryLock() { 
		//A writer lock can only be acquired if no locks are active.
		if ((holder.get() instanceof Writer) && holder.get().thread == Thread.currentThread())
			throw new IllegalStateException();
		if (this.holder.compareAndSet(null, new Writer(Thread.currentThread()) )) 
			return true;
		return false;
	}
	
	/** 
			unlock a write lock acquired for current thread.
	*/
	public void writerUnlock() { 
		//A writer lock can only be unlocked if current holder is a Writer and if the current thread is
		//the owner of the write lock.
		if (!(holder.get() instanceof Writer) || holder.get().thread != Thread.currentThread())
			throw new IllegalStateException();
		holder.set(null); 
	}
}
