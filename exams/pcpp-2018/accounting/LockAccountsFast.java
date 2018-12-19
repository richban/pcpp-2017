import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


public class LockAccountsFast implements Accounts {
    private int[] accounts;
    private int[] sums;
    private final Object[] locks;
    private final int lockCount;

    public LockAccountsFast(int n, int lockCount) {
        this.accounts = new int[n];
        this.lockCount = lockCount;
        this.locks = new Object[this.lockCount];
        this.sums = new int[this.lockCount];
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
      Thread thread = Thread.currentThread();
      final int h = getHash(thread);
      synchronized(locks[account]){
        return accounts[account];
      }
    }

    public int sumBalances() {
        int sum = 0;
        for (int i = 0; i < lockCount ; i++) {
          synchronized(locks[i]) {
              sum += sums[i];
          }
        }
        return sum;
    }

    public void deposit(int to, int amount) {
      Thread thread = Thread.currentThread();
      final int h = getHash(thread);
        synchronized(locks[to]) {
          final int hash = h % accounts.length;
          accounts[to] += amount;
          sums[hash] += amount;
        }
    }

    public void transfer(int from, int to, int amount) {
      accounts[from] -= amount;
      accounts[to] += amount;
    }

    public void transferAccount(Accounts other) {
        for (int i = 0; i < accounts.length; i++) {
            accounts[i] += other.get(i);
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
}
