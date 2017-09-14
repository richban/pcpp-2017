// For week 1
// rikj@itu.dk 2017-09-01
import java.util.concurrent.atomic.*;
public class TestStaticCounter {
    final static int threads = 2;
    final static long ir = 20_000_000;

    final static AtomicLong count = new AtomicLong(0L);

    public static void main(String args[] ) {
        Thread t[] = new Thread[ threads ];
        for(int k=0;k<t.length;k++) {
            final int me = k;
            t[k] = new Thread( () -> {
                    for(int j=0;j<ir;j++)
                        count.getAndIncrement();
                    System.out.println("fresh " + me + " stops: "+ count.get());
                });
        }
        System.out.println("main");
        for(int k=0;k<t.length;k++) {
            t[k].start();
        }
        System.out.println("main finished "+ ir*t.length);
    }
}
