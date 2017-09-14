/**
 * Exercise 2.4
 */

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class Memoizer0<A, V> implements Computable<A, V> {
  private final Map<A, V> cache = new ConcurrentHashMap<>();
  private final Computable<A, V> c;
  
  public Memoizer0(Computable<A, V> c) { this.c = c; }

  public V compute(A arg) throws
        InterruptedException {
    cache.computeIfAbsent(arg, (A argg) -> {
        try {
            return c.compute(argg);
        }
        catch (InterruptedException e) {
            System.err.println("A thread was stopped...");
            e.printStackTrace();
            return null;
        }
    });
    return cache.get(arg);
  }
}


public class CachedFactorsCounter {
    private static void exerciseFactorizer(Computable<Long, long[]> f)
            throws InterruptedException {
        final long START = 10_000_000_000L,
                   CHUNK = 5_000L,
                   RANGE = CHUNK * 4,
                   END = START + RANGE;
        final int THREADS_COUNT = 16;

        System.out.println(f.getClass());

        Thread[] threads = new Thread[THREADS_COUNT];

        for (int h = 0; h < THREADS_COUNT; ++h) {
            final long myStart = END + h * CHUNK,
                       myEnd = myStart + RANGE;

            threads[h] = new Thread(() -> {
                try {
                    for (long k = START; k < END; ++k) {
                        f.compute(k);
                    }
                    for (long k = myStart; k < myEnd; ++k) {
                        f.compute(k);
                    }
                }
                catch (InterruptedException e) {
                    System.err.println("A thread was stopped...");
                    e.printStackTrace();
                }
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }
    }
    
    public static void main(String[] args) {
        Factorizer f = new Factorizer();
        Computable<Long, long[]> c = null;

        switch (Integer.parseInt(args[0])) {
            case 0:
                c = new Memoizer0<>(f);
                break;
                
            case 1:
                c = new Memoizer1<>(f);
                break;
                
            case 2:
                c = new Memoizer2<>(f);
                break;
                
            case 3:
                c = new Memoizer3<>(f);
                break;
                
            case 4:
                c = new Memoizer4<>(f);
                break;
                
            case 5:
                c = new Memoizer5<>(f);
                break;

            default:
                System.err.println("No Memoizer class found for number " + args[0]);
                System.exit(1);
        }

        try {
            exerciseFactorizer(c);
        }
        catch (InterruptedException e) {
            System.err.println("A thread was stopped...");
            e.printStackTrace();
        }

        System.out.println("Total number of calls: " + f.getCount());        
    }
}
