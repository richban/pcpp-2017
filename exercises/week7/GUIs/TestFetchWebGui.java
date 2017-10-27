// For week 7
// sestoft@itu.dk * 2014-10-12

// General GUI stuff
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// For the SwingWorker subclass
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

// For downloading web pages
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

// For communicating with the progress bar
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class TestFetchWebGui {
  public static void main(String[] args) {
    badFetch();
    // goodFetch();
  }

  // (0) This version performs all the slow web access work on the
  // event thread.  This means that the GUI remains blocked until all
  // pages have been fetched, so the Cancel button can have no effect
  // (even if it had a listener) and the text area does not get
  // repainted and hence not updated with the results as they become
  // available.

  private static void badFetch() {
    final JFrame frame = new JFrame("TestFetchWebGui");
    final JPanel outerPanel = new JPanel(), 
      buttonPanel = new JPanel();
    final JButton fetchButton = new JButton("Fetch"), 
      cancelButton = new JButton("Cancel");
    frame.add(outerPanel);
    outerPanel.setLayout(new BorderLayout());
    buttonPanel.setLayout(new GridLayout(2, 1));
    buttonPanel.add(fetchButton);
    buttonPanel.add(cancelButton);
    outerPanel.add(buttonPanel, BorderLayout.EAST);
    final TextArea textArea = new TextArea(25, 80);
    textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    outerPanel.add(textArea, BorderLayout.WEST);
    fetchButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          for (String url : urls) {
            System.out.println("Fetching " + url);
            String page = getPage(url, 200);
            textArea.append(String.format("%-40s%7d%n", url, page.length()));
          }
        }});
    frame.pack(); frame.setVisible(true);
  }

  // (1) Use a SwingWorker subclass to perform the work in the
  // doInBackground method on a different thread, cause the event
  // thread to show the final result using the done method.  (2) Add a
  // progress bar that displays the fraction of web pages fetched so
  // far.  (3) Add the possibility of cancellation by testing
  // isCancelled() in the doInBackground method, and catch the
  // CancellationException in the done method.  (4) Display the
  // results as they become available by letting doInBackground call
  // the publish method, which will cause the event thread to sooner
  // or later run the process method.  In this case the done method
  // should not also write the results to the textArea.  Note that it
  // would be illegal for the worker thread to directly .append to the
  // textArea.

  private static void goodFetch() {
    final JFrame frame = new JFrame("TestFetchWebGui");
    final JPanel outerPanel = new JPanel(), 
      buttonPanel = new JPanel();
    final JButton fetchButton = new JButton("Fetch"), 
      cancelButton = new JButton("Cancel");
    frame.add(outerPanel);
    outerPanel.setLayout(new BorderLayout());
    buttonPanel.setLayout(new GridLayout(2, 1));
    buttonPanel.add(fetchButton);
    buttonPanel.add(cancelButton);
    outerPanel.add(buttonPanel, BorderLayout.EAST);
    final TextArea textArea = new TextArea(25, 80);
    textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    outerPanel.add(textArea, BorderLayout.WEST);
    // (1) Use a background thread, not the event thread, for work
    DownloadWorker downloadTask = new DownloadWorker(textArea);
    fetchButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          downloadTask.execute();
        }});
    // (2) Add a progress bar
    JProgressBar progressBar = new JProgressBar(0, 100);
    outerPanel.add(progressBar, BorderLayout.SOUTH);
    downloadTask.addPropertyChangeListener(new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent e) {
          if ("progress".equals(e.getPropertyName())) {
            progressBar.setValue((Integer)e.getNewValue());
          }}});
    // (3) Enable cancellation
    cancelButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          downloadTask.cancel(false);
        }});
    frame.pack(); frame.setVisible(true);
  }

  private static final String[] urls = 
  { "http://www.itu.dk", "http://www.di.ku.dk", "http://www.miele.de",
    "http://www.microsoft.com", "http://www.dr.dk",
    "http://www.vg.no", "http://www.tv2.dk", "http://www.google.com",
    "http://www.ing.dk", "http://www.dtu.dk", "http://www.eb.dk", 
    "http://www.nytimes.com", "http://www.guardian.co.uk", "http://www.lemonde.fr",   
    "http://www.welt.de", "http://www.dn.se", "http://www.heise.de", "http://www.wsj.com", 
    "http://www.bbc.co.uk", "http://www.dsb.dk", "http://www.bmw.com", "https://www.cia.gov" 
  };

  public static String getPage(String url, int maxLines) {
    try {
      // This will close the streams after use (JLS 8 para 14.20.3):
      try (BufferedReader in 
           = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<maxLines; i++) {
          String inputLine = in.readLine();
          if (inputLine == null)
            break;
          else
            sb.append(inputLine).append("\n");
        }
	return sb.toString();
      }
    } catch (IOException exn) { 
      System.out.println(exn); 
      return "";
    }
  }

  static class DownloadWorker extends SwingWorker<String,String> {
    private final TextArea textArea; 

    public DownloadWorker(TextArea textArea) {
      this.textArea = textArea;
    }

    // Called on a separate worker thread, not the event thread, and
    // hence cannot write to textArea.
    public String doInBackground() {
      StringBuilder sb = new StringBuilder();
      int count = 0;
      for (String url : urls) {
        // if (isCancelled())			    // (3)
        //   break;
	System.out.println("Fetching " + url);
        String page = getPage(url, 200),
          result = String.format("%-40s%7d%n", url, page.length());
        sb.append(result); // (1)
	//      setProgress((100 * ++count) / urls.length); // (2)
	//	publish(result); // (4)
      }
      return sb.toString();
    }
  
    // (4) Called on the event thread to process results previously
    // published by calls to the publish method.
    public void process(List<String> results) {
      for (String result : results)
        textArea.append(result);
    }

    // Called in the event thread when done() has terminated, whether
    // by completing or by being cancelled.
    public void done() {
      try { 
        textArea.append(get());
        textArea.append("Done"); 
      } catch (InterruptedException exn) { }
        catch (ExecutionException exn) { throw new RuntimeException(exn.getCause()); }
        catch (CancellationException exn) { textArea.append("Yrk"); }  // (3)
    }
  }
}

