import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

interface Histogram { 
  void increment(int bin);
  int getCount(int bin);
  int getSpan();
  int[] getBins();
  int getAndClear(int bin);
  void transferBins(Histogram hist);
}

public class CasHistogram implements Histogram {
    private final AtomicInteger[] bins;
    private int span;

    public CasHistogram(int span) {
        this.span = span;
        bins = new AtomicInteger[span]; {
            for (int i = 0; i < bins.length; i++) {
                bins[i] = new AtomicInteger(0);
            }
        }
    }

    public void increment(int bin) {
        int oldValue;
        do {
            oldValue = bins[bin].get();
        } while (!bins[bin].compareAndSet(oldValue, oldValue+1));
    }

    public int getCount(int bin) {
        return bins[bin].get();
    }

    public int getSpan() {
        return span;
    }

    public int[] getBins() {
        return IntStream.range(0, getSpan()).map((bin) -> getCount(bin)).toArray();
    }

    public int getAndClear(int bin) {
        int oldValue;
        do {
            oldValue = bins[bin].get();
        } while (!bins[bin].compareAndSet(oldValue, 0));
        return oldValue;
    }

    public void transferBins(Histogram hist) {
        for (int i = 0; i < hist.getSpan(); i++) {
            int oldValue, newValue;
            int delta = hist.getAndClear(i);
            do {
                oldValue = this.getCount(i);
                newValue = oldValue + delta;
            } while (!bins[i].compareAndSet(oldValue, newValue));
        }
    }
} 
