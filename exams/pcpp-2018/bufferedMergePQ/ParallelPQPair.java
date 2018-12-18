import java.util.function.Function;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.List;
import java.util.ArrayList;


public class ParallelPQPair implements PQPair {
  private static final ExecutorService executor = Executors.newCachedThreadPool();
  PQ left, right;

  public PQPair clone() {
      return new ParallelPQPair();
  }
  public void createPairParam(Parameters param, Function<Parameters,PQ> instanceCreator){
    List<Callable<PQ>> tasks = new ArrayList<Callable<PQ>>();

    tasks.add(() -> {
      return instanceCreator.apply( param.left());
    });

    tasks.add(() -> {
      return instanceCreator.apply( param.right());
    });

    try {
      List<Future<PQ>> futures = executor.invokeAll(tasks);
      left = futures.get(0).get();
      right = futures.get(1).get();
    } catch (InterruptedException | ExecutionException exn) {
      System.out.println("Interrupted: " + exn);
    }
  }

  public PQ getLeft() { return left; }
  public PQ getRight(){ return right; }
}
