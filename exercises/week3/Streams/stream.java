import java.util.stream.*;

public class stream {
  static final int N = 999_999_999;

  public static void sumStream() {
    double sum = IntStream.range(1, N)
                          .mapToDouble(n -> 1.0 / n)
                          .sum();
    System.out.printf("Sum = %20.16f%n", sum);
  }
  public static void main(String[] args) {
    sumStream();
  }
}
