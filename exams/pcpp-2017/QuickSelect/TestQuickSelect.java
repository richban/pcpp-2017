
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.function.IntFunction;
import java.util.function.IntToDoubleFunction;
import java.util.function.Function;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;


class TestQuickSelect {

  private static final ExecutorService executor
    = Executors.newCachedThreadPool();
    // = Executors.newWorkStealingPool();

  public static int medianSort(int[] inp) {
    int w[] = Arrays.copyOf(inp, inp.length);
    Arrays.sort(w);
    return w[w.length/2];
  }
  public static int medianPSort(int[] inp) {
    int w[] = Arrays.copyOf(inp, inp.length);
    Arrays.parallelSort(w);
    return w[w.length/2];
  }

  public static int partition(int[] w, int min, int max) {
    int p = min; // use w[p] as pivot
    int left=min+1, right = max-1;
    while(left <= right) {
      while( w[left] <= w[p] && left < right ) left++;
      while( w[right] > w[p] && left <= right ) right--;
      if(left >= right) break;
      int t=w[left]; w[left]=w[right]; w[right]=t;
    }
    int t=w[p]; w[p]=w[right]; w[right]=t;
    return right;
  }

  public static int quickSelect(int[] inp) {
    int w[] = Arrays.copyOf(inp, inp.length);
    return quickSelect(w,0,w.length,w.length/2);
  }
  public static int quickSelect(int[] w, int min, int max, int target) {
    int p = partition(w,min,max);
    if( p < target ) return quickSelect(w,p+1,max,target);
    if( p > target ) return quickSelect(w,min,p,target);
    return w[target]; // p==target
  }

  public static int quickSelectIt(int[] inp) {
    int w[] = Arrays.copyOf(inp, inp.length);
    int target = w.length/2;
    int p = -1, min=0, max=w.length;
    do{
      p = partition(w,min,max);
      if( p < target )  min=p+1;
      if( p > target )  max=p;
      //      System.out.println(" "+p+"   "+target);
    } while(p!=target);
    return w[p];
  }

  public static int quickCountRec(int[] inp, int target) {
    final int p=inp[0], n=inp.length;
    int  count=0;
    for(int i=1;i<n;i++) if(inp[i]<p) count++;
    if(count > target) {
      int m[] = new int[count];
      int j=0;
      for(int i=1;i<n;i++) if(inp[i]<p) m[j++]=inp[i];
      return quickCountRec(m,target);
    }
    if(count < target) {
      int m[] = new int[n-count-1];
      int j=0;
      for(int i=1;i<n;i++) if(inp[i]>=p) m[j++]=inp[i];
      return quickCountRec(m,target-count-1);
    }
    return p; // we are on target
  }

  public static int quickCountParRec(int[] inp, int target, int taskCount) {
    final int p=inp[0], n=inp.length;
    final int perTask = n / taskCount;
    List<Callable<Integer>> tasks_count = new ArrayList<Callable<Integer>>();

    for (int t=0; t<taskCount; t++) {
      final int from = perTask * t,
        to = (t+1 == taskCount) ? n : perTask * (t+1);
      tasks_count.add(() -> {
          int count = 0;
          for (int i=from; i<to; i++)
            if (inp[i]<p) count++;
          return count;
        });
    }

    int results = 0;

    try {
      List<Future<Integer>> futures = executor.invokeAll(tasks_count);
      for (Future<Integer> fut : futures)
        results += fut.get();
    } catch (InterruptedException | ExecutionException exn) {
      System.out.println("Interrupted: " + exn);
    }

    if (results == target ) return p;

    List<Callable<List<Integer>>> tasks = new ArrayList<>();
    int next_target = -1;

    if (results > target) {
      next_target = target;
      for (int t=0; t<taskCount; t++) {
        final int from = perTask * t,
          to = (t+1 == taskCount) ? n : perTask * (t+1);
        tasks.add(() -> {
            List<Integer> local_list = new ArrayList<>();
            for (int i=from; i<to; i++)
              if (inp[i]<p) local_list.add(inp[i]);
            return local_list;
          });
      }
    } else {
      next_target = target-results-1;
      for (int t=0; t<taskCount; t++) {
        final int from = perTask * t,
          to = (t+1 == taskCount) ? n : perTask * (t+1);
        tasks.add(() -> {
            List<Integer> local_list = new ArrayList<>();
            for (int i=from; i<to; i++)
              if (inp[i]>=p) local_list.add(inp[i]);
            return local_list;
          });
      }
    }

    List<Integer> next_input = new ArrayList<>();
    try {
      List<Future<List<Integer>>> futures = executor.invokeAll(tasks);
      for (Future<List<Integer>> fut : futures)
        next_input.addAll(fut.get());
    } catch (InterruptedException | ExecutionException exn) {
      System.out.println("Interrupted: " + exn);
    }

    int[] arr = next_input.stream().mapToInt(Integer::intValue).toArray();
    return quickCountParRec(arr, next_target, taskCount);
  }

  public static int quickCountRecParStr(int[] inp, int target, int taskCount) {
    final int p=inp[0], n=inp.length;
    final int perTask = n / taskCount;
    IntStream list_stream = Arrays.stream(inp);

    List<Callable<Long>> tasks_count = new ArrayList<Callable<Long>>();

    for (int t=0; t<taskCount; t++) {
      final int from = perTask * t,
        to = (t+1 == taskCount) ? n : perTask * (t+1);
      tasks_count.add(() -> {
          return list_stream.parallel().skip(from).limit(to).filter(i -> i < p).count();
        });
    }

    int results = 0;

    try {
      List<Future<Long>> futures = executor.invokeAll(tasks_count);
      for (Future<Long> fut : futures)
        results += fut.get();
        System.out.println(results);
    } catch (InterruptedException | ExecutionException exn) {
      System.out.println("Interrupted: " + exn);
    }

    if (results == target ) return p;

    List<Callable<List<Integer>>> tasks = new ArrayList<>();
    int next_target = -1;

    if (results > target) {
      next_target = target;
      for (int t=0; t<taskCount; t++) {
        final int from = perTask * t,
          to = (t+1 == taskCount) ? n : perTask * (t+1);
        tasks.add(() -> {
            List<Integer> local_list = new ArrayList<>();
            for (int i=from; i<to; i++)
              if (inp[i]<p) local_list.add(inp[i]);
            return local_list;
          });
      }
    } else {
      next_target = target-results-1;
      for (int t=0; t<taskCount; t++) {
        final int from = perTask * t,
          to = (t+1 == taskCount) ? n : perTask * (t+1);
        tasks.add(() -> {
            List<Integer> local_list = new ArrayList<>();
            for (int i=from; i<to; i++)
              if (inp[i]>=p) local_list.add(inp[i]);
            return local_list;
          });
      }
    }

    List<Integer> next_input = new ArrayList<>();
    try {
      List<Future<List<Integer>>> futures = executor.invokeAll(tasks);
      for (Future<List<Integer>> fut : futures)
        next_input.addAll(fut.get());
    } catch (InterruptedException | ExecutionException exn) {
      System.out.println("Interrupted: " + exn);
    }

    int[] arr = next_input.stream().mapToInt(Integer::intValue).toArray();
    return quickCountParRec(arr, next_target, taskCount);
  }

  public static int quickCountIt(int[] inp) {
    int p=-1, count=0, n=inp.length;
    int target = n/2;
    do {
      p=inp[0];
      count=0;
      n=inp.length;
      for(int i=1;i<n;i++) if(inp[i]<p) count++;
      if(count > target) {
        int m[] = new int[count];
        int j=0;
        for(int i=1;i<n;i++) if(inp[i]<p) m[j++]=inp[i];
        inp = m;
        continue;
      }
      if(count < target) {
        int m[] = new int[n-count-1];
        int j=0;
        for(int i=1;i<n;i++) if(inp[i]>=p) m[j++]=inp[i];
        inp =m;
        target=target-count-1;
        continue;
      }
      break;
    } while( true );
    return p; // we are on target
  }

  public static void main( String [] args ) {
    SystemInfo();
    int input_size = Integer.parseInt(args[0]);
    // SortingBenchmarksTasks(input_size, 32);
    SortingBenchmarksSize(input_size, 4);
  }

  private static int[] SetupSort(int size) {
    final int a[] = new int[size];
    Random rnd = new Random();
    int nrIt = 10;
    for(int ll=0;ll<nrIt;ll++) {
      rnd.setSeed(23434+ll); // seed
      for(int i=0;i<a.length;i++) a[i] = rnd.nextInt(4*a.length);
      final int ra = quickCountRec(a,a.length/2); //
      final int rb = medianPSort(a);
      if( ra !=rb ) {
        System.out.println(ll);
        System.out.println(ra);
        System.out.println(rb);
        System.exit(0);
      }
    }
    return a;
  }

  private static void SortingBenchmarksTasks(int input_size, int taskCount) {
    final int a[] = SetupSort(input_size);
    for (int t = 1; t <= taskCount; t++) {
      final int tasks = t;
      Mark8("parallel countRc",
             String.format("%8d", input_size),
             String.format("%3d", t),
             i -> quickCountParRec(a, a.length/2, tasks));
    }

  }

  private static void SortingBenchmarksSize(int input_size, int taskCount) {
    for (int size = 100; size <= input_size; size *= 2) {
      final int a[] = SetupSort(size);
      Mark8("parallel countRc",
             String.format("%8d", size),
             String.format("%3d", taskCount),
             i -> quickCountParRec(a, a.length/2, taskCount));
    }
    for (int size = 100; size <= input_size; size *= 2) {
      final int a[] = SetupSort(size);
      Mark8("serial-sort",
             String.format("%8d", size),
             "1",
             i -> medianSort(a));
    }
    for (int size = 100; size <= input_size; size *= 2) {
      final int a[] = new int[size];
      Mark8("parallel-sort",
            String.format("%8d", size),
            "1",
            i -> medianPSort(a));
    }
    for (int size = 100; size <= input_size; size *= 2) {
      final int a[] = new int[size];
      Mark8("serial-quickSelect",
            String.format("%8d", size),
            "1",
            i -> quickSelect(a));
    }
    for (int size = 100; size <= input_size; size *= 2) {
      final int a[] = new int[size];
      Mark8("ser-countIt",
            String.format("%8d", size),
            "1",
            i -> quickCountIt(a));
    }
    for (int size = 100; size <= input_size; size *= 2) {
      final int a[] = new int[size];
      Mark8("ser-countRc",
            String.format("%8d", size),
            "1",
            i -> quickCountRec(a,a.length/2));
    }
  }

  public static double Mark8(String msg, String info, String taskCount,
                              IntToDoubleFunction f, int n, double minTime) {
    int count = 1, totalCount = 0;
    double dummy = 0.0, runningTime = 0.0, st = 0.0, sst = 0.0;
    int size = Integer.parseInt(info.replaceAll("\\s+",""));
    do {
      count *= 2;
      st = sst = 0.0;
      for (int j=0; j<n; j++) {
        Timer t = new Timer();
        for (int i=0; i<count; i++) {
          dummy += f.applyAsDouble(i);
        }
        runningTime = t.check();
        double time = runningTime * 1e9 / count;
        st += time;
        sst += time * time;
        totalCount += count;
      }
    } while (runningTime < minTime && count < Integer.MAX_VALUE/2);
    double mean = st/n/size, sdev = Math.sqrt((sst - mean*mean*n)/(n-1))/size;
    System.out.printf("%-25s %s %s %15.1f ns %10.2f %10d%n", msg, info, taskCount, mean, sdev, count);
    return dummy / totalCount;
  }

  public static double Mark8(String msg, String info, String taskCount, IntToDoubleFunction f) {
    return Mark8(msg, info, taskCount, f, 5, 1);
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

}
