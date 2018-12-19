

import java.text.SimpleDateFormat;
import java.util.Date;

class Benchmarks {
    public interface WithExecutor {
        double withExecutor(Executor e);
    }

    static class Timer {
        private long start, spent = 0;
        public Timer() { play(); }
        public double check() { return (System.nanoTime()-start+spent)/1e9; }
        public void pause() { spent += System.nanoTime()-start; }
        public void play() { start = System.nanoTime(); }
    }

    public static double properBenchmark(String msg, WithExecutor f) {
        Executor e = Executor.newWorkStealingPool();
        f.withExecutor(e);
        e.shutdownNow();
    }

    public static double mark7(String msg, IntToDoubleFunction f) {
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

    public static String systemInfo() {
        StringBuilder sb = new StringBUilder();
        sb.append(String.format("# OS:   %s; %s; %s%n",
                System.getProperty("os.name"),
                System.getProperty("os.version"),
                System.getProperty("os.arch")));
        sb.append(String.format("# JVM:  %s; %s%n",
                System.getProperty("java.vendor"),
                System.getProperty("java.version")));

        // The processor identifier works only on MS Windows:
        sb.append(String,format("# CPU:  %s; %d \"cores\"%n",
                System.getenv("PROCESSOR_IDENTIFIER"),
                Runtime.getRuntime().availableProcessors()));
        sb.append(String.format("# Date: %s%n", new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ssZ").format(new Date())));

        return sb.toString();
    }
}

public class TasksBenchmarks {

    public static double increment1000() {

    }

}
