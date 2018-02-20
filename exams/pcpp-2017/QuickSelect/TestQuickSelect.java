
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


class TestQuickSelect {

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
    SortingBenchmarks(input_size);
  }

  private static void SetupSort(int[] a) {
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
  }

  private static void SortingBenchmarks(int input_size) {
    for (int size = 100; size <= input_size; size *= 2) {
      final int a[] = new int[size];
      Mark8Setup("serial sort",
                 String.format("%8d", size),
                 new Benchmarkable() {
                   public void setup() { SetupSort(a); }
                   public double applyAsDouble(int i)
                   { medianSort(a); return 0.0; } });
    }
  }

  public static double Mark8Setup(String msg, String info, Benchmarkable f,
                                  int n, double minTime) {
    int count = 1, totalCount = 0;
    double dummy = 0.0, runningTime = 0.0, st = 0.0, sst = 0.0;
    do {
      count *= 2;
      st = sst = 0.0;
      for (int j=0; j<n; j++) {
        Timer t = new Timer();
        for (int i=0; i<count; i++) {
          t.pause();
          f.setup();
          t.play();
          dummy += f.applyAsDouble(i);
        }
        runningTime = t.check();
        double time = runningTime * 1e9 / count;
        st += time;
        sst += time * time;
        totalCount += count;
      }
    } while (runningTime < minTime && count < Integer.MAX_VALUE/2);
    double mean = st/n, sdev = Math.sqrt((sst - mean*mean*n)/(n-1));
    System.out.printf("%-25s %s%15.1f ns %10.2f %10d%n", msg, info, mean, sdev, count);
    return dummy / totalCount;
  }

  public static double Mark8Setup(String msg, String info, Benchmarkable f) {
    return Mark8Setup(msg, info, f, 10, 0.25);
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
