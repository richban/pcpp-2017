/**
 * Exercise 1, question 2
 */

public class MyAtomicInteger {
    private volatile int value;

    public MyAtomicInteger(int init) {
        value = init;
    }

    public MyAtomicInteger() {
        this(0);
    }

    public synchronized int addAndGet(int amount) {
        return value += amount;
    }

    public synchronized int incAndGet() {
        return addAndGet(1);
    }

    public int get() {
        return value;
    }
}
