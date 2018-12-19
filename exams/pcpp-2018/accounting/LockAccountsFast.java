import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


public class LockAccountsFast implements Accounts {
    private int[] accounts;
    private int[] sums;
    private final Object[] locks;
    private final Object[] acclocks;
    private final int lockCount;

    public LockAccountsFast(int n, int lockCount) {
        this.accounts = new int[n];
        this.lockCount = lockCount;
        this.locks = new Object[this.lockCount];
        this.sums = new int[this.lockCount];
        this.acclocks = new Object[n];
        for (int account = 0; account < n; account++)
          this.acclocks[account] = new Object();
        for (int stripe = 0; stripe < this.lockCount; stripe++)
          this.locks[stripe] = new Object();
    }

    // Protect against poor hash functions and make non-negative
    private static int getHash(Thread t) {
      final int th = t.hashCode();
      // System.out.printf("th: %d\n", th);
      return (th ^ (th >>> 16)) & 0x7FFFFFFF;
    }

    public void init(int n) {
        this.accounts = new int[n];
    }

    public int get(int account) {
      synchronized (acclocks[account]) {
        return accounts[account];
      }
    }

    public int sumBalances() {
      AtomicInteger result = new AtomicInteger(0);
      lockAllAndThen(() -> {
        for (int stripe = 0; stripe < lockCount; stripe++) {
          result.getAndAdd(sums[stripe]);
        }
      });
      return result.get();
    }

    public void deposit(int to, int amount) {
      Thread thread = Thread.currentThread();
      final int h = getHash(thread);
      final int stripe = h % accounts.length;

      synchronized (acclocks[to]) {
        accounts[to] += amount;
      }
      synchronized (locks[stripe]) {
        sums[stripe] += amount;
      }
    }

    public void transfer(int from, int to, int amount) {
      synchronized (acclocks[from]) { accounts[from] -= amount; }
      synchronized (acclocks[to]) { accounts[to] += amount; }
    }

    public void transferAccount(Accounts other) {
        for (int i = 0; i < accounts.length; i++) {
          synchronized (acclocks[i]) {
            this.deposit(i, other.get(i));
          }
        }
    }

    public String toString() {
        String res = "";
        if (accounts.length > 0) {
            res = "" + accounts[0];
            for (int i = 1; i < accounts.length; i++) {
                res = res + " " + accounts[i];
            }
        }
        return res;
    }

    // Lock all stripes, perform the action, then unlock all stripes
    private void lockAllAndThen(Runnable action) {
      lockAllAndThen(0, action);
    }

    private void lockAllAndThen(int nextStripe, Runnable action) {
      if (nextStripe >= lockCount)
        action.run();
      else
        synchronized (locks[nextStripe]) {
          lockAllAndThen(nextStripe + 1, action);
        }
    }
}
