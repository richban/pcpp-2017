import java.util.stream.*;
import java.util.function.*;
import java.util.*;

public class Stream {
  static final int N = 999_999_999;

  private static DoubleStream makeNewStream() {
    return IntStream.range(1, N)
                          .mapToDouble(n -> 1.0 / n);
  }
  // Exercise 3.4.1
  public static void sumStream1() {
    Long start = System.currentTimeMillis();
    double sum = makeNewStream().sum();
    Long end = System.currentTimeMillis();

    System.out.printf("Sum = %20.16f%n" + " and it took : "
                      + (end - start) + " ms\n", sum);
  }

  // Exercise 3.4.2
  public static void sumStream2() {
    Long start = System.currentTimeMillis();
    double sum = makeNewStream().parallel().sum();
    Long end = System.currentTimeMillis();

    System.out.printf("Sum2 = %20.16f%n" + " and it took : "
                      + (end - start) + " ms with parallel\n", sum);
  }

  // Exercise 3.4.3
  public static void sumStandArray() {
    double sum3 = 0;
    Long start3 = System.currentTimeMillis();
    PrimitiveIterator.OfDouble iterator = makeNewStream().iterator();
    while(iterator.hasNext()){
      sum3 += iterator.nextDouble();
    }
    Long end3 = System.currentTimeMillis();

    System.out.printf("Sum3 = %20.16f%n" + " and it took : "
                      + (end3 - start3) + " ms with for-loop\n", sum3);
  }

  // Exercise 3.4.4
  // DoubleSupplier is an interface. Classes Implemet interfaces. which means
  // actual implementation of the interface method. Can't intanciiet an interface
  // Here I am creating Object which implements the DoubleSupplier interface
  // withoud creating the class.
  public static void sumStream3() {
    DoubleStream doublestream = DoubleStream.generate(new DoubleSupplier() {
      private double number = 1.0;
      public double getAsDouble() { return 1.0 / number++; }
    })
                              .limit(N);
    Long start4 = System.currentTimeMillis();
    double sum4 = doublestream.sum();
    Long end4 = System.currentTimeMillis();

    System.out.printf("Sum4 = %20.16f%n" + " and it took : "
                      + (end4 - start4) + " ms with parallel\n", sum4);
  }

  public static void sumStream4() {
    DoubleStream doublestream = DoubleStream.generate(new DoubleSupplier() {
      private double number = 1.0;
      public double getAsDouble() { return 1.0 / number++; }
    })
                              .limit(N);
    Long start4 = System.currentTimeMillis();
    double sum4 = doublestream.parallel().sum();
    Long end4 = System.currentTimeMillis();

    System.out.printf("Sum4 = %20.16f%n" + " and it took : "
                      + (end4 - start4) + " ms with parallel\n", sum4);
  }

  public static void main(String[] args) {
    sumStream1();
    sumStream2();
    sumStream3();
    sumStandArray();
    sumStream4();
  }
}
