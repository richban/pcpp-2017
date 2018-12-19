import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


public class LockAccounts implements Accounts {
    private int[] accounts;
    private final int lockCount;
    private final Object[] locks;
    private final AtomicIntegerArray serials;

    public LockAccounts(int n) {
        this.accounts = new int[n];
        this.lockCount = n;
        this.locks = new Object[this.lockCount];
        this.serials = new AtomicIntegerArray(this.accounts);
        for (int account = 0; account < this.lockCount; account++)
          this.locks[account] = new Object();
    }

    public void init(int n) {
        this.accounts = new int[n];
    }

    public int get(int account) {
      synchronized(locks[account]){
        return accounts[account];
      }
    }

    public int sumBalances() {
        int sum = 0;
        for (int i = 0; i < accounts.length; i++) {
            sum += accounts[i];
        }
        return sum;
    }

    public void deposit(int to, int amount) {
        synchronized(locks[to]) {
          accounts[to] += amount;
        }
    }

    public void transfer(int from, int to, int amount) {
      if (serials.get(from) < serials.get(to)) {
        synchronized (locks[from]) { // from <= to
          synchronized (locks[to]) {
            accounts[from] -= amount;
            accounts[to] += amount;
          }
        }
      } else {
        synchronized (locks[to]) { // to < from
          synchronized (locks[from]) {
            accounts[from] -= amount;
            accounts[to] += amount;
          }
        }
      }
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
