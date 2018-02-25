import java.util.Iterator;
import java.util.Random;
import java.io.*;
import akka.actor.*;

// class MESSAGES --------------------------------------------------

class StartTransferMessage implements Serializable {
    public final ActorRef bank;
    public final ActorRef from;
    public final ActorRef to;

    public StartTransferMessage(ActorRef bank, ActorRef from, ActorRef to) {
        this.bank = bank;
        this.from = from;
        this.to = to;
    }
}

class TransferMessage implements Serializable {
    public final double amount;
    public final ActorRef from;
    public final ActorRef to;

    public TransferMessage(double amount, ActorRef from, ActorRef to) {
        this.amount = amount;
        this.from = from;
        this.to = to;
    }
}

class DepositMessage implements Serializable {
    public final double amount;

    public DepositMessage(double amount) {
        this.amount = amount;
    }
}

class PrintBalanceMessage implements Serializable {}

// class ACTORS --------------------------------------------------

class AccountActor extends UntypedActor {
    private static long nextID = 0;

    private long ID;
    private double balance;

    public AccountActor() {
        ID = AccountActor.nextID++;
    }

    public void onReceive(Object m)
            throws Exception {
        if (m instanceof DepositMessage) {
            balance += ((DepositMessage) m).amount;
        }
        else if (m instanceof PrintBalanceMessage) {
            System.out.printf("Account %d - balance: %f\n", ID, balance);
        }
    }
}

class BankActor extends UntypedActor {
    public void onReceive(Object m)
            throws Exception {
        if (m instanceof TransferMessage) {
            TransferMessage msg = (TransferMessage) m;
            msg.from.tell(new DepositMessage(-msg.amount), ActorRef.noSender());
            msg.to.tell(new DepositMessage(+msg.amount), ActorRef.noSender());
        }
    }
}

class ClerkActor extends UntypedActor {
    private static final int SPAMS_COUNT = 100;

    private static void spamBank(ActorRef bank, ActorRef from, ActorRef to) {
        long seed = System.nanoTime() + Thread.currentThread().hashCode();
        Iterator<Double> iterator = new Random(seed)
            .doubles(ClerkActor.SPAMS_COUNT)
            .iterator();

        while (iterator.hasNext()) {
            double amount = iterator.next();
            bank.tell(new TransferMessage(amount, from, to), ActorRef.noSender());
        }
    }

    public void onReceive(Object m)
            throws Exception {
        if (m instanceof StartTransferMessage) {
            StartTransferMessage msg = (StartTransferMessage) m;
            ClerkActor.spamBank(msg.bank, msg.from, msg.to);
        }
    }
}

// -- MAIN --------------------------------------------------

public class ABC { // Demo showing how things work:
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("ABCSystem");

        // Actors creation

        final ActorRef[] accounts = new ActorRef[] {
                system.actorOf(Props.create(AccountActor.class), "account0"),
                system.actorOf(Props.create(AccountActor.class), "account1")
            };

        final ActorRef[] banks = new ActorRef[] {
                system.actorOf(Props.create(BankActor.class), "bank0"),
                system.actorOf(Props.create(BankActor.class), "bank1")
            };

        final ActorRef[] clerks = new ActorRef[] {
                system.actorOf(Props.create(ClerkActor.class), "clerk0"),
                system.actorOf(Props.create(ClerkActor.class), "clerk1")
            };

        // Sending start messages

        for (int k = 0; k < clerks.length; ++k) {
            StartTransferMessage msg = new StartTransferMessage(
                banks[k],
                accounts[k],
                accounts[accounts.length - k - 1]
            );
            clerks[k].tell(msg, ActorRef.noSender());
        }

        try {
            System.out.println("Press return to inspect...");
            System.in.read();

            for (ActorRef account : accounts) {
                account.tell(new PrintBalanceMessage(), ActorRef.noSender());
            }

            System.out.println("Press return to terminate...");
            System.in.read();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            system.shutdown();
        }
    }
}
