package intellij;

public class PrinterTester {

	public static void main(String [ ] args)
	{
	     //Printer p = new Printer();

	     Thread t1 = new Thread(() -> {while (true) Printer.print(); });
	     Thread t2 = new Thread(() -> {while (true) Printer.print(); });
	     
	     t1.start();
	     t2.start();
	}
	
}
