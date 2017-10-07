// For week 5
// sestoft@itu.dk * 2014-09-19

import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class TestDownload {

    private static final String[] urls = 
    { "http://www.itu.dk", "http://www.di.ku.dk", "http://www.miele.de",
        "http://www.microsoft.com", "http://www.amazon.com", "http://www.dr.dk",
        "http://www.vg.no", "http://www.tv2.dk", "http://www.google.com",
        "http://www.ing.dk", "http://www.dtu.dk", "http://www.eb.dk", 
        "http://www.nytimes.com", "http://www.guardian.co.uk", "http://www.lemonde.fr",     
        "http://www.welt.de", "http://www.dn.se", "http://www.heise.de", "http://www.wsj.com", 
        "http://www.bbc.co.uk", "http://www.dsb.dk", "http://www.bmw.com", "https://www.cia.gov" 
    };

    // Here lied main(). moved down in assignment section

    public static String getPage(String url, int maxLines) throws IOException {
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
    }

    /**************************************************

    Assignment code

    **************************************************/

    public static Map<String, String> getPages(String[] urls, int maxLines) throws IOException {
        Map<String, String> result = new HashMap<>();
        for (String url : urls) {
            result.put(url, getPage(url, maxLines));
        }
        return result;
    }

    public static Map<String, String> getPagesParallel(String[] urls, int maxLines) throws IOException, ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newWorkStealingPool();
        Map<String, Future<String>> tasks = new HashMap<>();
        Map<String, String> result = new HashMap<>();

        for (String url : urls) {
            tasks.put(url, executor.submit(() -> {
                return getPage(url, maxLines);
            }));
        }

        for (Map.Entry<String, Future<String>> pair : tasks.entrySet()) {
            result.put(pair.getKey(), pair.getValue().get());
        }

        return result;
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        Timer timer = new Timer();
        // Map<String, String> fetched = getPages(urls, 200);
        Map<String, String> fetched = getPagesParallel(urls, 200);
        System.out.printf("Pages took %d ns to load\n", timer.check());
        for (Map.Entry<String, String> data : fetched.entrySet()) {
            System.out.printf("URL: %s, #char fetched %d\n", data.getKey(), data.getValue().length());
        }
    }
}

class Timer {
  private long start, spent = 0;
  public Timer() { play(); }
  public long check() { return (System.nanoTime()-start+spent); }
  public void pause() { spent += System.nanoTime()-start; }
  public void play() { start = System.nanoTime(); }
}
