/**
 * Exercise 2.1, question 1
 */

public class SerialFactorsCounter {
    public static void main(String[] args) {
        long upperBound = 5_000_000;
        long factorsCount = 0;

        for (int k = 0; k < upperBound; ++k) {
            factorsCount += FactorsCounter.countFactors(k);
        }

        System.out.println("Total number of factors: " + factorsCount);            
    }
}
