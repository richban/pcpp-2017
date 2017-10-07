/**
 * Exercise 4.4
 */

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntToDoubleFunction;

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

public class TestMemoizers {
    public static double Mark7(String msg, IntToDoubleFunction f) {
        int n = 10, count = 1, totalCount = 0;
        double dummy = 0.0, runningTime = 0.0, st = 0.0, sst = 0.0;
        do { 
          count *= 2;
          st = sst = 0.0;
          for (int j=0; j<n; j++) {
            Timer t = new Timer();
            for (int i=0; i<count; i++) 
              dummy += f.applyAsDouble(i);
            runningTime = t.check();
            double time = runningTime / count;
            st += time; 
            sst += time * time;
            totalCount += count;
          }
        } while (runningTime < 25e7 && count < Integer.MAX_VALUE/2);
        double mean = st/n, sdev = Math.sqrt((sst - mean*mean*n)/(n-1));
        System.out.printf("%-25s %15.1f ns %10.2f %10d%n", msg, mean, sdev, count);
        return dummy / totalCount;
    }

    public static void SystemInfo() {
        System.out.printf("# OS:   %s; %s; %s%n", 
                          System.getProperty("os.name"), 
                          System.getProperty("os.version"), 
                          System.getProperty("os.arch"));
        System.out.printf("# JVM:  %s; %s%n", 
                          System.getProperty("java.vendor"), 
                          System.getProperty("java.version"));
        // The processor identifier works only on MS Windows:
        System.out.printf("# CPU:  %s; %d \"cores\"%n", 
                          System.getenv("PROCESSOR_IDENTIFIER"),
                          Runtime.getRuntime().availableProcessors());
        java.util.Date now = new java.util.Date();
        System.out.printf("# Date: %s%n", 
          new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(now));
    }
    
    private static void exerciseFactorizer(Computable<Long, long[]> f) {
        final long START = 10_000_000_000L,
                   CHUNK = 2_000L,
                   RANGE = CHUNK * 4,
                   END = START + RANGE;
        final int THREADS_COUNT = 16;

        Mark7(f.getClass().getName(), n -> {
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

            try {
                for (Thread thread : threads) {
                    thread.join();
                }
            }
            catch (InterruptedException e) {
                System.err.println("A thread was stopped...");
                e.printStackTrace();
            }
    
            return 0;
        });
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

        SystemInfo();

        exerciseFactorizer(c);
    }
}

class Timer {
  private long start, spent = 0;
  public Timer() { play(); }
  public double check() { return (System.nanoTime()-start+spent); }
  public void pause() { spent += System.nanoTime()-start; }
  public void play() { start = System.nanoTime(); }
}
