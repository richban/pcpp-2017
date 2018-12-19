import java.util.stream.*;
import java.util.function.*;
import java.util.Random;
import java.util.Arrays;

// RUN THIS FILE AS FOLLOWS
// rm -f *.class && javac Runner.java -cp ./../lib/multiverse-core-0.7.0.jar STMAccounts.java && java -cp ./../lib/multiverse-core-0.7.0.jar:. Runner

// The Multiverse transactional memory library:
// import org.multiverse.api.references.*;
// import static org.multiverse.api.StmUtils.*;

public class Runner {
    public static void main(String[] args) {
        final int n = 5_000_000;
        // testAccounts(new UnsafeAccounts(n), n);
        // concurrentTestQ1(new UnsafeAccounts(n), n);
        // concurrentTestQ2(new UnsafeAccounts(n), n);

        // testAccounts(new LockAccounts(n), n);
        // concurrentTestQ1(new LockAccounts(n), n);
        // concurrentTestQ2(new LockAccounts(n), n);
        // deadlockTestQ3(new LockAccounts(100), 100);

        // concurrentTestQ2(new LockAccountsFast(n, 2), n);

        // concurrentTestQ1(new STMAccounts(n), n);
        // concurrentTestQ2(new STMAccounts(n), n);
        // deadlockTestQ4(new STMAccounts(n), n);

        // testAccounts(new CASAccounts(n), n);
        // concurrentTestQ1(new CASAccounts(n), n);
        // concurrentTestQ2(new CASAccounts(n), n);

        final int numberOfTransactions = 1000;
        // applyTransactionsLoop(n, numberOfTransactions, () -> new UnsafeAccounts(n));
        applyTransactionsLoop(n, numberOfTransactions, () -> new STMAccounts(n));
        applyTransactionsLoop(n, numberOfTransactions, () -> new CASAccounts(n));
        // applyTransactionsCollect(n, numberOfTransactions, () -> new CASAccounts(n));
    }

    public static void testAccounts(Accounts accounts, final int n) {
        if (n <= 2) {
            System.out.println("Accounts must be larger that 2 for this test to work");
            assert (false); // test only supports larger accounts that 2.
            return;
        }
        assert (accounts.sumBalances() == 0);
        accounts.deposit(n - 1, 55);
        assert (accounts.get(n - 1) == 55);
        assert (accounts.get(n - 2) == 0);
        assert (accounts.sumBalances() == 55);
        accounts.deposit(0, 45);
        assert (accounts.sumBalances() == 100);

        accounts.transfer(0, n - 1, -10);
        assert (accounts.sumBalances() == 100);
        assert (accounts.get(n - 1) == 45);
        assert (accounts.get(0) == 55);
        accounts.transfer(1, n - 1, 10);
        assert (accounts.get(n - 1) == 55);
        assert (accounts.get(1) == -10);
        assert (accounts.get(0) == 55);
        assert (accounts.sumBalances() == 100);

        accounts.transferAccount(accounts);
        assert (accounts.get(n - 1) == 55 * 2);
        assert (accounts.get(1) == -10 * 2);
        assert (accounts.get(0) == 55 * 2);
        assert (accounts.sumBalances() == 200);

        System.out.printf(accounts.getClass() + " passed sequential tests\n");
    }

    public static void concurrentTestQ1(Accounts accounts, final int n) {
      if (n <= 2) {
          System.out.println("Accounts must be larger that 2 for this test to work");
          assert (false); // test only supports larger accounts that 2.
          return;
      }
      assert (accounts.sumBalances() == 0);
      Thread clerk1 = new Thread(() -> {
        for (int i = 0; i < n; i++) {
          accounts.deposit(i, 100);
        }
      });
      Thread clerk2 = new Thread(() -> {
        for (int i = 0; i < n; i++) {
          accounts.deposit(i, -100);
        }
      });
      clerk1.start(); clerk2.start();
      // System.out.println(accounts.get(0));
      try { clerk1.join(); clerk2.join(); } catch (InterruptedException exn) { }
      System.out.println("Sum is " + accounts.sumBalances() + " and should be " + 0);
    }

    public static void concurrentTestQ2(Accounts accounts, final int n) {
      if (n <= 2) {
          System.out.println("Accounts must be larger that 2 for this test to work");
          assert (false); // test only supports larger accounts that 2.
          return;
      }
      assert (accounts.sumBalances() == 0);
      final int counts = n;
      Thread clerk1 = new Thread(() -> {
        for (int i = 0; i < counts; i++) {
          accounts.deposit(i, 1);
        }
      });
      Thread clerk2 = new Thread(() -> {
        for (int i = 0; i < counts; i++) {
          accounts.deposit(i, 1);
        }
      });
      clerk1.start(); clerk2.start();
      // System.out.println(accounts.get(0));
      try { clerk1.join(); clerk2.join(); } catch (InterruptedException exn) { }
      System.out.println("Sum is " + accounts.sumBalances() + " and should be " + 2*counts);
    }

    public static void deadlockTestQ3(Accounts accounts, final int n) {
      final Random rnd = new Random();
      final int transfers = 20;

      if (n <= 2) {
          System.out.println("Accounts must be larger that 2 for this test to work");
          assert (false); // test only supports larger accounts that 2.
          return;
      }
      accounts.deposit(0, 3000); accounts.deposit(1, 2000);

      Thread clerk1 = new Thread(() -> {
      for (int i=0; i<transfers; i++)
        accounts.transfer(0, 1, rnd.nextInt(10000));
      });

      Thread clerk2 = new Thread(() -> {
        for (int i=0; i<transfers; i++)
          accounts.transfer(1, 0, rnd.nextInt(10000));
      });

      clerk1.start(); clerk2.start();
      // We occasionally print the account balances during the transfer:
      for (int i=0; i<40; i++) {
        try { Thread.sleep(10); } catch (InterruptedException exn) { }
        // Locking both accounts is necessary to avoid reading the
        // balance in the middle of a transfer.
        System.out.println(accounts.sumBalances());
      }
      // The auditor prints the account balance sum when the clerks are finished:
      try { clerk1.join(); clerk2.join(); } catch (InterruptedException exn) { }
        System.out.println("\nFinal sum is " + accounts.sumBalances());
    }

    public static void deadlockTestQ4(Accounts accounts, final int n) {
      final Random rnd = new Random();
      final int transfers = 2_000_000;

      accounts.deposit(0, 3000); accounts.deposit(1, 2000);
      Thread clerk1 = new Thread(() -> {
      for (int i=0; i<transfers; i++)
        accounts.transfer(0, 1, rnd.nextInt(10000));
      });

      Thread clerk2 = new Thread(() -> {
        for (int i=0; i<transfers; i++)
          accounts.transfer(1, 0, rnd.nextInt(10000));
      });

      clerk1.start(); clerk2.start();
      // We may occasionally print the account balances during the transfers:
      for (int i=0; i<40; i++) {
        try { Thread.sleep(100); } catch (InterruptedException exn) { }
        // atomic(() -> { System.out.println(accounts.get(0) + accounts.get(1)); });
        // accounts.deposit(0, 10);
        // long sum = atomic(() -> accounts.get(0) + accounts.get(1));
        // System.out.println(sum);
      }
      // The auditor prints the account balance sum when the clerks are finished:
      try { clerk1.join(); clerk2.join(); } catch (InterruptedException exn) { }
      System.out.println(accounts.get(0) + accounts.get(1));
    }

    // Question 1.7.1
    private static void applyTransactionsLoop(int numberOfAccounts, int numberOfTransactions,
            Supplier<Accounts> generator) {
        // remember that if "from" is -1 in transaction then it is considered a deposit
        // otherwise it is a transfer.
        final Accounts accounts = generator.get();
        Stream<Transaction> transaction = IntStream.range(0, numberOfTransactions).parallel()
                .mapToObj((i) -> new Transaction(numberOfAccounts, i));
        // implement applying each transaction by using a for-loop
        // Modify it to run with a parallel stream.
        System.out.printf("numberOfAccounts: %d", numberOfTransactions);
        System.out.println();
        // transaction.forEach(t -> System.out.println(t.toString()));
        System.out.println(accounts.sumBalances());
        transaction.parallel().forEach(t -> {
          if (t.from == -1) {
            // System.out.println(t.toString());
            accounts.deposit(t.to, t.amount);
          } else {
            // System.out.println(t.toString());
            accounts.transfer(t.from, t.to, t.amount);
          }
        });

        // System.out.println(Arrays.toString(accounts.getAccounts()));
        System.out.println(accounts.sumBalances());
        for(int i = 0; i < 5; i++) {
          System.out.println(accounts.get(i));
        }
        // System.out.println(accounts.getSpan());

    }
 //
 //    // Question 1.7.2
 //    private static void applyTransactionsCollect(int numberOfAccounts, int numberOfTransactions,
 //                                                 Supplier<Accounts> generator) {
 //        // remember that if "from" is -1 in transaction then it is considered a deposit
 //        // otherwise it is a transfer.
 //        Stream<Transaction> transactions = IntStream.range(0, numberOfTransactions).parallel()
 //                .mapToObj((i) -> new Transaction(numberOfAccounts, i));
 //
 //        // Implement applying each transaction by using the collect stream operator.
 //        // Modify it to run with a parallel stream.
 // // YOUR CODE GOES HERE
 //    }
}
