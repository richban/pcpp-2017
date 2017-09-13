// For week 2
// sestoft@itu.dk * 2014-08-29
import java.util.Arrays;
import java.util.concurrent.atomic.*;
class TestCountFactors {
  public static void main(String[] args) {
    final int range = 5_000_000; 
	final Histogram5 histogram = new Histogram5(range);
	int threadCount = 10;
	int part = range / threadCount;
	Thread[] threadArray = new Thread[threadCount];
	int curThread = 0;
    for (int i=0; i<range; i+=part)
	{
	 final int from = i;
	 final int to = i+part;
     threadArray[curThread] = new Thread(() -> { 
     for (int p=from; p<to; p++)
		histogram.increment(countFactors(p));
      });
	  threadArray[curThread++].start();
	}	 
	
    for (int i=0; i<threadCount; i++)
		try { threadArray[i].join(); } catch (InterruptedException exn) { }
	
	int sum = 0;
    for (int b=0; b<histogram.getSpan() && sum < range; b++) {
	int count = histogram.getCount(b);
    System.out.printf("%d : %d%n",b, count);
	sum += count;
	}
  }

  public static int countFactors(int p) {
    if (p < 2) 
      return 0;
    int factorCount = 1, k = 2;
    while (p >= k * k) {
      if (p % k == 0) {
	factorCount++;
	p /= k;
      } else 
	k++;
    }
    return factorCount;
  }
}

interface Histogram {
  public void increment(int bin);
  public int getCount(int bin);
  public int getSpan();
  public int[] getBins();
}


class Histogram2 implements Histogram {
  private final int[] counts;
  public Histogram2(int span) {
    this.counts = new int[span];
  }
  public synchronized void increment(int bin) {
    counts[bin] = counts[bin] + 1;
  }
  public synchronized int getCount(int bin) {
    return counts[bin];
  }
  public int getSpan() {
    return counts.length;
  }
  public int[] getBins() {
    return Arrays.copyOf(counts,getSpan());
  }
}

class Histogram3 implements Histogram {
  private final AtomicInteger[] counts;
  public Histogram3(int span) {
    this.counts = new AtomicInteger[span];
  }
  AtomicInteger getBin(int binIndex)
  {
	  AtomicInteger bin = counts[binIndex];
	  if (bin == null) {
		bin = new AtomicInteger(0);
		counts[binIndex] = bin;
	  }
	  return bin;
  }
  public void increment(int bin) {
    getBin(bin).getAndIncrement();
  }
  public int getCount(int bin) {
    return getBin(bin).get();
  }
  public int getSpan() {
    return counts.length;
  }
  public int[] getBins() {
	int[] array = new int[getSpan()];
	for (int i = 0;i < array.length;i++) array[i] = getCount(i);
    return array;
  }
}
class Histogram4 implements Histogram {
  private final AtomicIntegerArray counts;
  public Histogram4(int span) {
    this.counts = new AtomicIntegerArray(span);
  }
  public void increment(int bin) {
    counts.getAndIncrement(bin);
  }
  public int getCount(int bin) {
    return counts.get(bin);
  }
  public int getSpan() {
    return counts.length();
  }
  public int[] getBins() {
	int[] array = new int[getSpan()];
	for (int i = 0;i < array.length;i++) array[i] = getCount(i);
    return array;
  }
}

class Histogram5 implements Histogram {
  private final LongAdder[] counts;
  public Histogram5(int span) {
    this.counts = new LongAdder[span];
  }
  LongAdder getBin(int binIndex)
  {
	  LongAdder bin = counts[binIndex];
	  if (bin == null) {
		bin = new LongAdder();
		counts[binIndex] = bin;
	  }
	  return bin;
  }
  public void increment(int bin) {
    getBin(bin).increment();
  }
  public int getCount(int bin) {
    return (int)getBin(bin).sum();
  }
  public int getSpan() {
    return counts.length;
  }
  public int[] getBins() {
	int[] array = new int[getSpan()];
	for (int i = 0;i < array.length;i++) array[i] = getCount(i);
    return array;
  }
}