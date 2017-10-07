// Week 5

// Counting primes using the .NET Task Parallel Library.  In general
// much less verbose and easier to use out of the box than Java's
// executors, but Java's facilities actually seem more efficient.

// sestoft@itu.dk * 2014-08-31, 2015-02-11

using System;
using System.Threading;
using System.Threading.Tasks;

public class TestCountPrimesTasks {
  public static void Main(String[] args) {
    SystemInfo();
    int range = 100000;
    Mark6("countSequential", (int i) => { 
        return countSequential(range);
      });
    Mark6("countParallelN", (int i) => { 
        return countParallelN(range, 1000);
      });
    Mark6("countParallelNLocal", (int i) => { 
        return countParallelNLocal(range, 10);
      });
    Console.WriteLine(Mark7("countSequential", (int i) => { 
          return countSequential(range);
        }));
    for (int c=1; c<=16; c++) {
      Mark7(String.Format("countParallelN      {0,6}", c), 
            (int i) => { return countParallelN(range, c);
      });
    }
    for (int c=1; c<=16; c++) {
      Mark7(String.Format("countParallelNLocal {0,6}", c), 
              (int i) => { return countParallelNLocal(range, c);
      });
    }
  }

  private static bool isPrime(int n) {
    int k = 2;
    while (k * k <= n && n % k != 0)
      k++;
    return n >= 2 && k * k > n;
  }

  // Sequential solution
  private static long countSequential(int range) {
    long count = 0;
    int from = 0, to = range;
    for (int i=from; i<to; i++)
      if (isPrime(i)) 
        count++;
    return count;
  }

  // General parallel solution, using void Tasks with shared variable
  private static long countParallelN(int range, int taskCount) {
    int perTask = range / taskCount;
    LongCounter lc = new LongCounter();
    Parallel.For(0, taskCount, t =>
      { int from = perTask * t, 
            to = (t+1==taskCount) ? range : perTask * (t+1); 
        for (int i=from; i<to; i++)
          if (isPrime(i))
            lc.increment();
      });
    return lc.get();
  }

  // General parallel solution, using multiple threads
  private static long countParallelNLocal(int range, int taskCount) {
    int perTask = range / taskCount;
    long[] results = new long[taskCount];
    Parallel.For(0, taskCount, t =>
      { int from = perTask * t, 
            to = (t+1==taskCount) ? range : perTask * (t+1); 
        long count = 0;
        for (int i=from; i<to; i++)
          if (isPrime(i))
            count++;
        results[t] = count;
      });
    long result = 0;
    for (int t=0; t<taskCount; t++) 
      result += results[t];
    return result;
  }

  // --- Benchmarking infrastructure ---

  // NB: Modified to show microseconds instead of nanoseconds

  public static double Mark6(String msg, Func<int,double> f) {
    int n = 10, count = 1, totalCount = 0;
    double dummy = 0.0, runningTime = 0.0, st = 0.0, sst = 0.0;
    do {
      count *= 2;
      st = sst = 0.0;
      for (int j=0; j<n; j++) {
        Timer t = new Timer();
        for (int i=0; i<count; i++) 
          dummy += f(i);
        runningTime = t.Check();
        double time = runningTime * 1e6 / count; // microseconds
        st += time; 
        sst += time * time;
        totalCount += count;
      }
      double mean = st/n, sdev = Math.Sqrt((sst - mean*mean*n)/(n-1));
      Console.WriteLine("{0,-25} {1,15:F1} us {2,10:F2} {3,10:D}", msg, mean, sdev, count);
    } while (runningTime < 0.25 && count < Int32.MaxValue/2);
    return dummy / totalCount;
  }

  public static double Mark7(String msg, Func<int,double> f) {
    int n = 10, count = 1, totalCount = 0;
    double dummy = 0.0, runningTime = 0.0, st = 0.0, sst = 0.0;
    do {
      count *= 2;
      st = sst = 0.0;
      for (int j=0; j<n; j++) {
        Timer t = new Timer();
        for (int i=0; i<count; i++) 
          dummy += f(i);
        runningTime = t.Check();
        double time = runningTime * 1e6 / count; // microseconds
        st += time; 
        sst += time * time;
        totalCount += count;
      }
    } while (runningTime < 0.25 && count < Int32.MaxValue/2);
    double mean = st/n, sdev = Math.Sqrt((sst - mean*mean*n)/(n-1));
    Console.WriteLine("{0,-25} {1,15:F1} us {2,10:F2} {3,10:D}", msg, mean, sdev, count);
    return dummy / totalCount;
  }

  private static void SystemInfo() {
    Console.WriteLine("# OS          {0}", 
      Environment.OSVersion.VersionString);
    Console.WriteLine("# .NET vers.  {0}",   
      Environment.Version);
    Console.WriteLine("# 64-bit OS   {0}",   
      Environment.Is64BitOperatingSystem);
    Console.WriteLine("# 64-bit proc {0}",   
      Environment.Is64BitProcess);
    Console.WriteLine("# CPU         {0}; {1} \"cores\"",   
      Environment.GetEnvironmentVariable("PROCESSOR_IDENTIFIER"),
      Environment.ProcessorCount); 
    Console.WriteLine("# Date        {0:s}", 
      DateTime.Now);
  }
}

// Crude timing utility ----------------------------------------

public class Timer {
  private readonly System.Diagnostics.Stopwatch stopwatch
    = new System.Diagnostics.Stopwatch();
  public Timer() { Play(); }
  public double Check() { return stopwatch.ElapsedMilliseconds / 1000.0; }
  public void Pause() { stopwatch.Stop(); }
  public void Play() { stopwatch.Start(); }
}

class LongCounter {
  private long count = 0;
  public void increment() {
    lock (this) {
      count = count + 1;
    }
  }
  public long get() { 
    lock (this) {
      return count;
    } 
  }
}
