import java.util.function.Function;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.List;
import java.util.ArrayList;


public class ParallelPQPair implements PQPair {
  private static final ExecutorService executor = Executors.newCachedThreadPool();
  // Cast the object to its class type
  ThreadPoolExecutor pool = (ThreadPoolExecutor) executor;
  PQ left, right;

  public PQPair clone() {
      return new ParallelPQPair();
  }
  public void createPairParam(Parameters param, Function<Parameters,PQ> instanceCreator){
    List<Callable<PQ>> tasks = new ArrayList<Callable<PQ>>();

    //Stats before tasks execution
    // System.out.println("Largest executions: " + pool.getLargestPoolSize());
    // System.out.println("Maximum allowed threads: " + pool.getMaximumPoolSize());
    // System.out.println("Current threads in pool: " + pool.getPoolSize());
    // System.out.println("Currently executing threads: " + pool.getActiveCount());
    // System.out.println("Total number of threads(ever scheduled): " + pool.getTaskCount());

    // Create a task for the left data structure
    tasks.add(() -> {
      // System.out.println("Running Task! Thread Name: " + Thread.currentThread().getName());
      return instanceCreator.apply( param.left());
    });
    // Create a task for the right data structure
    tasks.add(() -> {
      // System.out.println("Running Task! Thread Name: " + Thread.currentThread().getName());
      return instanceCreator.apply( param.right());
    });

    try {
      List<Future<PQ>> futures = executor.invokeAll(tasks);
      // Wait for all tasks to complete
      left = futures.get(0).get();
      right = futures.get(1).get();

      //Stats after tasks execution
      // System.out.println("Core threads: " + pool.getCorePoolSize());
      // System.out.println("Largest executions: " + pool.getLargestPoolSize());
      // System.out.println("Maximum allowed threads: " + pool.getMaximumPoolSize());
      // System.out.println("Current threads in pool: " + pool.getPoolSize());
      // System.out.println("Currently executing threads: " + pool.getActiveCount());
      // System.out.println("Total number of threads(ever scheduled): " + pool.getTaskCount());

    } catch (InterruptedException | ExecutionException exn) {
      System.out.println("Interrupted: " + exn);
    }

  }

  public PQ getLeft() { return left; }
  public PQ getRight(){ return right; }
}
