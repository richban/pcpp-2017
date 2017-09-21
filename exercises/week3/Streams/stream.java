import java.util.stream.*;

public class Stream {
  static final int N = 999_999_999;

  public static void sumStream() {
    Long start = System.currentTimeMillis();
    double sum = IntStream.range(1, N)
                          .mapToDouble(n -> 1.0 / n)
                          .sum();
    Long end = System.currentTimeMillis();

    Long start2 = System.currentTimeMillis();
    double sum2 = IntStream.range(1, N)
                          .mapToDouble(n -> 1.0 / n)
                          .sum();
    Long end2 = System.currentTimeMillis();

    System.out.printf("Sum = %20.16f%n" + " and it took : "
                      + (end - start) + "ms", sum);

    System.out.printf("Sum = %20.16f%n" + " and it took : "
                      + (end2 - start2) + "ms with parallel", sum2);
  }
  public static void main(String[] args) {
    sumStream();
  }
}
