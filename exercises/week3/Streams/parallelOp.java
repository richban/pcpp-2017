import java.util.Arrays;

public class parallelOp {
  static final int N = 10_000_000;

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

  public static void initArray(){
    int[] a = new int[N];
    Arrays.parallelSetAll(a, i -> { return isPrime(i) ? 1 : 0; });
  }

  public static void main(String[] args) {
    initArray();
  }
}
