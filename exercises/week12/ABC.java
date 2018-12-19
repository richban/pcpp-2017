import java.util.Random; import java.io.*; import akka.actor.*;

// -- MESSAGES --------------------------------------------------
class StartTransferMessage implements Serializable {
	public final ActorRef Bank;
	public final ActorRef From;
	public final ActorRef To;
	public StartTransferMessage()
	{
		Bank = null;
		From = null;
		To = null;
	}

	public StartTransferMessage(ActorRef bank,ActorRef from, ActorRef to)
	{
		Bank = bank;
		From = from;
		To = to;
	}
}
class TransferMessage implements Serializable {
	public final int Amount;
	public final ActorRef From;
	public final ActorRef To;
	public TransferMessage()
	{
		Amount = 0;
		From = null;
		To = null;
	}
	public TransferMessage(int amount,ActorRef from, ActorRef to)
	{
		Amount = amount;
		From = from;
		To = to;
	}
 }
class DepositMessage implements Serializable {
	public final int Amount;
	public DepositMessage()
	{
		Amount = 0;
	}
	public DepositMessage(int amount)
	{
		Amount = amount;
	}
}
class PrintBalanceMessage implements Serializable { }

// -- ACTORS --------------------------------------------------
class AccountActor extends UntypedActor {
	private int Balance;
	public void onReceive(Object o) throws Exception {
		 if (o instanceof PrintBalanceMessage) {
			 System.out.format("Balance = %d\n",Balance);
		 } else
		 if (o instanceof DepositMessage) {
			 DepositMessage message = (DepositMessage) o;
			 Balance += message.Amount;
		 }
	 }
 }
class BankActor extends UntypedActor {
	public void onReceive(Object o) throws Exception {
		 if (o instanceof TransferMessage) {
			TransferMessage message = (TransferMessage) o;
			message.From.tell(new DepositMessage(-message.Amount), ActorRef.noSender());
			message.To.tell(new DepositMessage(message.Amount), ActorRef.noSender());
		 }
	 }
 }
class ClerkActor extends UntypedActor {
	void NTransfer(Random rand, int n, ActorRef bank, ActorRef from, ActorRef to)
	{
		for (int i = 0; i < n; i++)
		{
			int randomAmount = rand.nextInt(n);
			bank.tell(new TransferMessage(randomAmount,from,to), ActorRef.noSender());
		}
	}
	public void onReceive(Object o) throws Exception {
		 if (o instanceof StartTransferMessage) {
			 long seed = System.currentTimeMillis() + this.hashCode();
			 Random rand = new Random(seed);
			 StartTransferMessage message = (StartTransferMessage) o;
			 NTransfer(rand, 100, message.Bank,message.From,message.To);
		 }
	 }

 }

// -- MAIN --------------------------------------------------
public class ABC { // Demo showing how things work:
 public static void main(String[] args) throws Exception {
	 final ActorSystem system = ActorSystem.create("ABCSystem");
	 /* TODO (CREATE ACTORS AND SEND START MESSAGES) */
	 final ActorRef a1 =
		system.actorOf(Props.create(AccountActor.class), "account1");
	final ActorRef a2 =
		system.actorOf(Props.create(AccountActor.class), "account2");
	 final ActorRef b1 =
		system.actorOf(Props.create(BankActor.class), "bank1");
	 final ActorRef b2 =
		system.actorOf(Props.create(BankActor.class), "bank2");
	 final ActorRef c1 =
		system.actorOf(Props.create(ClerkActor.class), "clerk1");
	 final ActorRef c2 =
		system.actorOf(Props.create(ClerkActor.class), "clerk2");

	 try {
		 c1.tell(new StartTransferMessage(b1,a1,a2), ActorRef.noSender());
		 c2.tell(new StartTransferMessage(b2,a2,a1), ActorRef.noSender());
		 System.out.println("Press return to inspect...");
		 System.in.read();
		 a1.tell(new PrintBalanceMessage(), ActorRef.noSender());
		 a2.tell(new PrintBalanceMessage(), ActorRef.noSender());
		 Thread.sleep(1000);

		 /* TODO (INSPECT FINAL BALANCES) */
		 System.out.println("Press return to terminate...");
		 System.in.read();
	 } catch(IOException e) {
		e.printStackTrace();
	 } finally {
		system.shutdown();
	 }
 }
}
