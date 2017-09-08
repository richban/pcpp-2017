package intellij;

public class Printer {
	
	public static void print() {
			synchronized (Printer.class) 
			{
				System.out.print("-");
				try { Thread.sleep(50); } catch (InterruptedException exn) { }
				System.out.print("|");
			}
		}
}
