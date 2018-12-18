import java.util.function.Function;
import java.util.List;
import java.util.ArrayList;


public class ParallelPQPair implements PQPair {
  PQ left, right;

  public PQPair clone() {
      return new ParallelPQPair();
  }
  public void createPairParam(Parameters param, Function<Parameters,PQ> instanceCreator){
    Thread t1 = new Thread(() -> {
      left = instanceCreator.apply( param.left());
    });
    Thread t2 = new Thread(() -> {
      right = instanceCreator.apply( param.right());
    });
    t1.start(); t2.start();
    try {
      t1.join(); t2.join();
    } catch (InterruptedException exn) {
      System.out.println("Interrupted: " + exn);
    }

  }

  public PQ getLeft() { return left; }
  public PQ getRight(){ return right; }
}
