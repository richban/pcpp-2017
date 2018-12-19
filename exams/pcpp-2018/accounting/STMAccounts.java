// Compile and run like this under Linux and MacOS:
//   javac -cp ./lib/multiverse-core-0.7.0.jar STMAccounts.java
//   java -cp ./lib/multiverse-core-0.7.0.jar:. STMAccounts

import org.multiverse.api.references.*;
import static org.multiverse.api.StmUtils.*;
import java.util.stream.IntStream;


interface Accounts {
    // (Re)initializes n accounts with balance 0 each.
    public void init(int n);

    // Returns the balance of account "account"
    public int get(int account);

    // Returns the sum of all balances.
    public int sumBalances();

    // Change the balance of account "to", by amount;
    // negative amount is allowed (a withdrawel)
    public void deposit(int to, int amount);

    // Transfer amount from account "from" to account "to"
    public void transfer(int from, int to, int amount);

    // Transfer all the balances from other to this accounts object.
    public void transferAccount(Accounts other);
}

public class STMAccounts implements Accounts {
    private TxnInteger[] accounts;

    public STMAccounts(int n) {
      accounts = new TxnInteger[n];
      for (int i = 0; i < accounts.length; i++) {
        accounts[i] = newTxnInteger(0);
      }
    }

    public void init(int n) {
      accounts = new TxnInteger[n];
      for (int i = 0; i < accounts.length; i++) {
        accounts[i] = newTxnInteger(0);
      }
    }

    public int getSpan() {
      return accounts.length;
    }

    public int get(int account) {
        return atomic(() -> accounts[account].get());
    }

    // Big transaction without consistency loss
    public int sumBalances() {
      return atomic(() -> {
        int sum = 0;
        for (int i = 0; i < accounts.length; i++) {
            sum += accounts[i].get();
        }
        return sum;
      });
    }

    public void deposit(int to, int amount) {
      atomic(() -> {
        accounts[to].increment(amount);
      });
    }

    public void transfer(int from, int to, int amount) {
      atomic(() -> {
        accounts[from].increment(-amount);
        accounts[to].increment(+amount);
      });
    }

    public void transferAccount(Accounts other) {
        for (int i = 0; i < getSpan(); i++) {
          final int index = i;
          atomic(() -> accounts[index].getAndSet(other.get(index)));
        }
    }

    public int[] getAccounts() {
      return IntStream.range(0, getSpan()).map((acc) -> accounts[acc].get()).toArray();
    }
}
