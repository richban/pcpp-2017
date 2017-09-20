// Week 3
// sestoft@itu.dk * 2015-09-09

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.*;
import java.util.Optional;

public class TestWordStream {
  public static void main(String[] args) {
    String filename = "usr/share/dict/words";
    System.out.println(readWords(filename).count());
	//3.3 - 2: Pipeline that prints 100 words.
	System.out.println("#Exercise 3.3 - 2");
	readWords(filename).limit(100).forEach(i -> System.out.println(i));
	
	//3.3 - 3: Pipeline that prints words with 22+ letters.
	System.out.println("#Exercise 3.3 - 3");
	readWords(filename).filter(x -> x.length() >= 22).forEach(i -> System.out.println(i));
	
	//3.3 - 4: Pipeline that prints some word with 22+ letters.
	System.out.println("#Exercise 3.3 - 4");
	System.out.println(readWords(filename).filter(x -> x.length() >= 22).findAny().orElse(""));
	
	//3.3 - 5: Pipeline that prints all palindrome words.
	System.out.println("#Exercise 3.3 - 5");
	readWords(filename).filter(x -> isPalindrome(x)).forEach(i -> System.out.println(i));
	
	//3.3 - 6: Pipeline that prints all palindrome words in parallel.
	System.out.println("#Exercise 3.3 - 6");
	readWords(filename).parallel().filter(x -> isPalindrome(x)).forEach(i -> System.out.println(i));
	
	//3.3 - 7: Pipeline that maps stream of words into steam of their lengths, print minimum, maximum, average word length.
	System.out.println("#Exercise 3.3 - 7"); 
	System.out.println(readWords(filename).mapToInt(x -> x.length()).min().orElse(0));
	System.out.println(readWords(filename).mapToInt(x -> x.length()).max().orElse(0));
	System.out.println(readWords(filename).mapToInt(x -> x.length()).average().orElse(0));
	
	//3.3 - 8: Pipeline that groups words based on word length, print groups.
	System.out.println("#Exercise 3.3 - 8"); 
	readWords(filename).collect(Collectors.groupingBy(w -> w.length(), Collectors.counting())).forEach((id,count)-> System.out.println(count + " words of length " + id));
	
	//3.3 - 9: print first 100 words as letters treeMap
	System.out.println("#Exercise 3.3 - 9"); 
	readWords(filename).limit(100).forEach(i -> System.out.println(letters(i)));
	
	//3.3 - 10: use tree map stream and reduce to count numbers of "e" occurances
	System.out.println("#Exercise 3.3 - 10"); 
	System.out.println(readWords(filename).map(i -> letters(i)).reduce(0, (a,b) -> Optional.ofNullable(b.get('e')).orElse(0) + a, (a,b) -> a+b));
	
	//3.3 - 11: use words letters, groupingBy and collect to print all anagrams.
	System.out.println("#Exercise 3.3 - 11"); 
	readWords(filename).collect(Collectors.groupingBy(l -> letters(l),Collectors.mapping(item->item, Collectors.toList()))).forEach((id,words)-> { if (words.size() > 1) System.out.println(words); });
	//print number of anagram sets
	System.out.println(readWords(filename).collect(Collectors.groupingBy(l -> letters(l),Collectors.mapping(item->item, Collectors.toList()))).entrySet().stream().mapToInt(entry-> (entry.getValue().size() > 1) ? 1 : 0).sum());
	
	//3.3 - 12: make parralel version of 11.
	System.out.println("#Exercise 3.3 - 12"); 
	//readWords(filename).parallel().collect(Collectors.groupingBy(l -> letters(l),Collectors.mapping(item->item, Collectors.toList()))).forEach((id,words)-> { if (words.size() > 1) System.out.println(words); });
	
	//3.3 - 13: use groupingByConcurrent.
	System.out.println("#Exercise 3.3 - 13"); 
	readWords(filename).parallel().collect(Collectors.groupingByConcurrent(l -> letters(l),Collectors.mapping(item->item, Collectors.toList()))).forEach((id,words)-> { if (words.size() > 1) System.out.println(words); });
	
  }

  public static Stream<String> readWords(String filename) {
    try {
      BufferedReader reader = new BufferedReader(new FileReader(filename));
      // TO DO: Implement properly
	  //3.3 - 1: Complete readWords
      return reader.lines(); 
    } catch (IOException exn) { 
      return Stream.<String>empty();
    }
  }

  public static boolean isPalindrome(String s) {
    // TO DO: Implement properly
	//3.3 - 5: Complete isPalindrome
    return IntStream.range(0, s.length() % 2 == 0 ? s.length()/ 2 : (s.length()-1) / 2)
	.allMatch(i -> s.charAt(i) == s.charAt(s.length() - i-1)); 
  }

  public static Map<Character,Integer> letters(String s) {
    Map<Character,Integer> res = new TreeMap<>();
    // TO DO: Implement properly
	//3.3 - 9: Complete letters
	s.toLowerCase().chars().mapToObj(c -> (char)c).collect(Collectors.groupingBy(c -> c, 
         Collectors.counting())).forEach((c,count) -> res.put(c,count.intValue()));
    return res;
  }
}
