import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class CASAccounts implements Accounts {
    private final AtomicInteger[] accounts;
    private int sum;

    public CASAccounts(int n) {
      accounts = new AtomicInteger[n];
      for (int i = 0; i < accounts.length; i++) {
        accounts[i] = AtomicInteger(0);
      }
    }

    public void init(int n) {
      accounts = new AtomicInteger[n];
      for (int i = 0; i < accounts.length; i++) {
        accounts[i] = AtomicInteger(0);
      }
    }

    public int get(int account) {
        return accounts[account].get();
    }

    public int sumBalances() {
        for (int i = 0; i < accounts.length; i++) {
          final int index = i;
          sum += accounts[i];
        }
        return sum;
    }

    public void deposit(int to, int amount) {
      int oldValue;
      do {
        oldValue = accounts[to].get();
      } while(!accounts[to].compareAndSet(oldValue, oldValue + amount));
    }

    public void transfer(int from, int to, int amount) {
        accounts[from] -= amount;
        accounts[to] += amount;
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
      }
    }
}
