import java.util.Arrays;

class parallelOps {
  static final int N = 10_000_001;
  static int[] a = new int[N];

  private boolean isPrime(int n) {
    int k = 2;
    while (k * k <= n && n % k != 0)
      k++;
    return n >= 2 && k * k > n;
  }

  public static void setAll() {
    Arrays.parallelSetAll(a, i -> isPrime(i) ? 1 : 0);
  }

  public static void sumAll() {
    Arrays.parallelPrefix(a, 0, N, (p, s) -> p + s);
    System.out.println(a[N]);
  }
}
