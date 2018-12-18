import java.util.stream.*;
import java.util.function.*;

public class Runner {
    public static void main(String[] args) {
        final int n = 100;
        testAccounts(new UnsafeAccounts(n), n);

        final int numberOfTransactions = 1000;
        applyTransactionsLoop(n, numberOfTransactions, () -> new UnsafeAccounts(n));
        applyTransactionsCollect(n, numberOfTransactions, () -> new UnsafeAccounts(n));
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
 // YOUR CODE GOES HERE 
    }

    // Question 1.7.2
    private static void applyTransactionsCollect(int numberOfAccounts, int numberOfTransactions,
                                                 Supplier<Accounts> generator) {
        // remember that if "from" is -1 in transaction then it is considered a deposit
        // otherwise it is a transfer.
        Stream<Transaction> transactions = IntStream.range(0, numberOfTransactions).parallel()
                .mapToObj((i) -> new Transaction(numberOfAccounts, i));

        // Implement applying each transaction by using the collect stream operator.
        // Modify it to run with a parallel stream.
 // YOUR CODE GOES HERE 
    }
}
