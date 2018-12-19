import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class CASAccounts implements Accounts {
    private AtomicInteger[] accounts;
    private AtomicInteger sum;

    public CASAccounts(int n) {
      accounts = new AtomicInteger[n];
      sum = new AtomicInteger(0);
      for (int i = 0; i < accounts.length; i++) {
        accounts[i] = new AtomicInteger(0);
      }
    }

    public void init(int n) {
      accounts = new AtomicInteger[n];
      for (int i = 0; i < accounts.length; i++) {
        accounts[i] = new AtomicInteger(0);
      }
    }

    public int get(int account) {
        return accounts[account].get();
    }

    public int getSpan() {
      return accounts.length;
    }

    public int sumBalances() {
      return sum.get();
    }

    public void deposit(int to, int amount) {
      int oldValue;
      do {
        oldValue = accounts[to].get();
      } while(!accounts[to].compareAndSet(oldValue, oldValue + amount));
      sum.getAndAdd(amount);
    }

    public void transfer(int from, int to, int amount) {
      int oldValue;
      int newValue;

      do {
        oldValue = accounts[from].get();
        newValue = oldValue - amount;
      } while(!accounts[from].compareAndSet(oldValue, newValue));
      do {
        oldValue = accounts[to].get();
        newValue = oldValue + amount;
      } while(!accounts[to].compareAndSet(oldValue, newValue));
    }

    public void transferAccount(Accounts other) {
      for (int i = 0; i < accounts.length; i++) {
        final int index = i;
        int oldValue, newValue;
        int delta = other.get(index);
        do {
          oldValue = this.get(index);
          newValue = delta + oldValue;
        } while(!accounts[index].compareAndSet(oldValue, newValue));
        sum.getAndAdd(other.get(index));
      }
    }

    public int[] getAccounts() {
      return IntStream.range(0, getSpan()).map((acc) -> accounts[acc].get()).toArray();
    }
}
