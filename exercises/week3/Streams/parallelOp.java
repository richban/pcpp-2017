import java.util.Arrays;

public class parallelOp {
  static final int N = 10_000_001;
  static int[] a = new int[N];

  public static boolean isPrime(int n) {
    //check if n is a multiple of 2
    if (n%2==0) return false;
    //if not, then just check the odds
    for(int i=3;i*i<=n;i+=2) {
        if(n%i==0)
            return false;
    }
    return true;
  }

  public static void setAll(){
    Arrays.parallelSetAll(a, i -> { return isPrime(i) ? 1 : 0; });
  }

  public static void sumAll(){
    Arrays.parallelPrefix(a, 0, N, (p, s) -> p + s);
    System.out.println(a[10_000_000]);
  }

  public static void main(String[] args) {
    setAll();
    sumAll();
  }
}
