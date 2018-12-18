import java.util.function.Function;  
import java.util.Random;
import java.util.Arrays;

// Test your bufferedPQ from here.
public class Runner {
    // for optional command line arguments at variable positions. Very useful for my debugging cycle on the command line; allows java style int constants with _
    public static int useArg(String[] args,int pos, int def) {
        if( args.length > pos ) return Integer.parseInt(args[pos].replace("_",""));
        return def;
    }

    // my command line looks like >>> rm -f *.class &&javac Runner.java && java Runner 3445 5_000 4 99
    public static void main(String[] args) {
        final int seed = useArg(args,0,45678);
        final int n = useArg(args,1,10_000_000);
        final int extractFract = useArg(args,2,4);       
        
        final int bufLen = useArg(args,3,20);
        final int maxDepth = useArg(args,99,4);
        final int cutOff = 4;

        final int extract = n / extractFract;

        // keep only what you need when you create a file with your experimental results
        System.out.printf("n %d  s %d, extract %d  bufLen %d  maxDepth %d  cutOff %d%n",n,seed,extract,bufLen,maxDepth,cutOff);
        SystemInfo();
        
        int[] a = generateRandom(n,seed); // useful to be able to compare output of the different implementations
        //        int[] a = new int[n]; for(int i=0;i<n;i++) a[i] = n-i;

        timePQ(a,extract,(x)-> new OneBufferPQ       (new Parameters(x,0,x.length,0,bufLen,cutOff,maxDepth,false,  null)),                "One Serial");
        timePQ(a,extract,(x)-> new OneBufferPQ       (new Parameters(x,0,x.length,0,bufLen,cutOff,maxDepth,true,   null)),                "One Parallel");

        timePQ(a,extract,(x)-> new BufferedPQ        (new Parameters(x,0,x.length,0,bufLen,cutOff,maxDepth,false,  new SerialPQPair())),  "BufferedPQ Ser/Serial");

        // // the following works if you happen to come up with something that looks like my solution; you can try to do this or you can adjust the calls -- up to you
        // timePQ(a,extract,(x)-> new BufferedPQ        (new Parameters(x,0,x.length,0,bufLen,cutOff,maxDepth,false,  new ParallelPQPair())),"BufferedPQ Par/Serial");
        // timePQ(a,extract,(x)-> new BufferedPQ        (new Parameters(x,0,x.length,0,bufLen,cutOff,maxDepth,true,new SerialPQPair())),  "BufferedPQ Ser/Parallel");
        // timePQ(a,extract,(x)-> new BufferedPQ        (new Parameters(x,0,x.length,0,bufLen,cutOff,maxDepth,true,new ParallelPQPair())),"BufferedPQ Par/Parallel");
        // timePQ(a,extract,(x)-> new BufferedPQSolution(new Parameters(x,0,x.length,0,bufLen,cutOff,maxDepth,false,  new SerialPQPair())),  "Solution Ser/Serial");
        // timePQ(a,extract,(x)-> new BufferedPQSolution(new Parameters(x,0,x.length,0,bufLen,cutOff,maxDepth,true,new SerialPQPair())),  "Solution Par/Serial");
        
        // timePQ(a,extract,(x)-> new BoundedBufferMerge(new Parameters(x,0,x.length,0,bufLen,cutOff,maxDepth, false ,new ParallelPQPair())),"Merge Par/Par");
        // timePQ(a,extract,(x)-> new BoundedBufferThreadMerge(new Parameters(x,0,x.length,0,bufLen,cutOff,maxDepth,false,new SerialPQPair())),"Merge Thread");
    }

    // in the following, there are some things that I found very useful for debugging.  Adjust to what you need -- or kick it out if it gets in the way
    private static void timePQ(int[] data, int extractLen, Function<int[],PQ> myPQconstr,String display) {
        Timer t = new Timer();
        //  printArray(data);

        int [] x = data.clone();
        // printArray(x);
        // Arrays.sort(x);
        // printArray(x);

        PQ queue = myPQconstr.apply(data);
        //System.out.println("----------");
        // queue.print(0);
        // try {
        //     Thread.sleep(10);
        // } catch (Exception e) {};
        queue.start();
        // try {
        //     Thread.sleep(1000);
        // } catch (Exception e) {};
        // queue.print(0);
        double ti = t.check();
        //        System.out.printf("(%9.3f)%n", ti);
        int[] result = getSortedElementsFromPQ(queue, extractLen); //data.length/4);
        
        double time = t.check();

        assert(checkIsSorted(result));
        System.out.printf("%d ",result[result.length-1]); // &0xff);
        //        printArray(result);
        //queue.print(0);
        queue.shutDown();
        
        System.out.printf("%-30s Real time: %9.3f (%9.3f)%n", display, time,ti);
    }

    // generate an array of size random elements
    private static int[] generateRandom(int size, int seed) {
        int[] a = new int[size];
        Random rng = new Random(seed);
        for (int i = 0; i < a.length; i++) {
            a[i] = rng.nextInt(); // & 0xfff;  // do this if you want readable numbers for debugging
        }
        return a;
    }
    
    // check if a is sorted in increasing order.
    private static boolean checkIsSorted(int[] a) {
        int last = a[0];
        for (int i = 1; i < a.length; i++) {
            if (a[i]<last)
                return false;
            last = a[i];
        }
        return true;
    }

    // get n sorted elements from queue by continuesly popping elements.
    private static int[] getSortedElementsFromPQ(PQ queue, int n) {
        int[] a = new int[n];
        int i = 0;
        while ( i<n) { // !queue.isEmpty() &&
            a[i] = queue.getMin();
            //System.out.println("++++ " + a[i]);
            //queue.print(0);
            i++;
        }
        
        return a;
    }

    private static void printArray(int[] arr) {
        if (arr != null) {
            int n = arr.length;
            //if(n>20) n=20;
            for (int i = 0; i < n; i++) {
                System.out.print(" " + arr[i]);
            }
            System.out.println();
        }
    }

    // ----------------------------------------------------------------------



    // ----------------------------------------------------------------------



    // ----------------------------------------------------------------------



    // ----------------------------------------------------------------------

    // DO NOT MODIFY ANYTHING BELOW THIS LINE

    private static void SystemInfo() {
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

    public static class Timer {
        private long start = 0, spent = 0;
        public Timer() { play(); }
        public double check() { return (start==0 ? spent : System.nanoTime()-start+spent)/1e9; }
        public void pause() { if (start != 0) { spent += System.nanoTime()-start; start = 0; } }
        public void play() { if (start == 0) start = System.nanoTime(); }
    }
}
