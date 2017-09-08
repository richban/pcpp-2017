/**
 * This class contains the computaion-
 * intensive code that required parallelization
 */

public class FactorsCounter {
    public static int countFactors(int p) {
        if (p < 2) {
            return 0;
        }

        int factorCount = 1, k = 2;

        while (p >= k * k) {
            if (p % k == 0) {
                factorCount++;
                p /= k;
            }
            else {
                k++;
            }
        }
        
        return factorCount;
  }
}
