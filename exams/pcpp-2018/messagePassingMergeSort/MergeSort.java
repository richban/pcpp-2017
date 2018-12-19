// COMPILE:
// javac -cp scala.jar:akka-actor.jar MergeSort.java
// RUN:
// java -cp scala.jar:akka-actor.jar:akka-config.jar:. MergeSort

import java.io.*;
import akka.actor.*;
import java.util.*;


public class MergeSort {
  public static void main(String[] args) {
    final ActorSystem system = ActorSystem.create("System");
    final ActorRef starter = system.actorOf(Props.create(StartActor.class), "Starter");
    starter.tell(new StartMessage(), ActorRef.noSender());
    try { System.out.println("Press return to terminate..."); System.in.read();
    } catch(IOException e) {
      e.printStackTrace();
    } finally { system.shutdown(); }
  }
}

// -- MESSAGES ------------------------------------------------

class StartMessage implements Serializable {
  public StartMessage() {}
}

class SplitMessage implements Serializable {
  public SplitMessage() {}
}

class SortMessage implements Serializable {
  public final ActorRef caller;
  public final int[] arr;
  public SortMessage(int[] arr, ActorRef caller) {
    this.arr = arr;
    this.caller = caller;
  }
  public int[] getSortMessage() { return arr; }
}

class CallerMessage implements Serializable {
  public final ActorRef caller;
  public CallerMessage(ActorRef caller) { this.caller = caller; }
}

class SortedMessage implements Serializable {
  public final int[] result;
  public SortedMessage(int[] result) {
    this.result = result;
  }
  public int[] getSortedMessage() { return result; }
}

// -- ACTORS --------------------------------------------------

class SorterActor extends UntypedActor {
  private ActorRef left, right;
  int[] arrToSort;

  public void onReceive(Object o) throws Exception {
    if (o instanceof SortMessage) {
      SortMessage s = (SortMessage) o;
      arrToSort = s.arr;
      if (arrToSort.length > 1) {
        ActorRef merger = getContext().actorOf(Props.create(MergerActor.class), "Merger");
        merger.tell(new CallerMessage(s.caller), ActorRef.noSender());
        // System.out.println("Spawn Left/Right Sorter;");
        left = getContext().actorOf(Props.create(SorterActor.class), "left");
        right = getContext().actorOf(Props.create(SorterActor.class), "right");

        // Sort
        int mid = arrToSort.length / 2;
        int[] l = new int[mid];
        int[] r = new int[arrToSort.length - mid];
        for (int i = 0; i < mid; i++) l[i] = arrToSort[i];
        for (int i = mid; i < arrToSort.length; i++) r[i - mid] = arrToSort[i];


        left.tell(new SortMessage(l, merger), ActorRef.noSender());
        right.tell(new SortMessage(r, merger), ActorRef.noSender());

      } else {
        s.caller.tell(new SortedMessage(arrToSort), ActorRef.noSender());
      }
    }
  }
}

class MergerActor extends UntypedActor {
  ActorRef caller;
  int[] left;


  private int[] merge(int[] l, int[] r) {
    int[] a  = new int[l.length+r.length];
    int left = l.length;
    int right = r.length;
    int i = 0, j = 0, k = 0;

    while (i < left && j < right) {
      if (l[i] <= r[j]) {
        a[k++] = l[i++];
      }
      else {
        a[k++] = r[j++];
      }
    }

    while (i < left) {
      a[k++] = l[i++];
    }

    while (j < right) {
      a[k++] = r[j++];
    }

    return a;
  }

  public void onReceive(Object o) throws Exception {
    if (o instanceof CallerMessage) {
      caller = ((CallerMessage) o).caller;
    } else if (o instanceof SortedMessage) {
      if (caller == null) throw new Exception("no caller address!!!");
      if (left == null) {
        SortedMessage l = ((SortedMessage) o);
        left = l.result;
      } else {
        SortedMessage r = ((SortedMessage) o);
        int[] result = merge(left, r.result);
        System.out.println("Merged: " + Arrays.toString(result));
        caller.tell(new SortedMessage(result), ActorRef.noSender());
        getContext().stop(getSelf()); // die!
      }
    }
  }
}

class TestActor extends UntypedActor {
  ActorRef caller;
  public final int arr[] = { 6, 5, 3, 1, 8, 7, 2, 4 };

  public static void printArray(int arr[]) {
    System.out.println("RESULT: " + Arrays.toString(arr));
  }

  public void onReceive(Object o) throws Exception {
    if (o instanceof CallerMessage) {
      caller = ((CallerMessage) o).caller;
      // SortList & Send Result to X - getSelf()
      caller.tell(new SortMessage(arr, getSelf()), ActorRef.noSender());
    } else if (o instanceof SortedMessage) {
      SortedMessage sortedMessage = (SortedMessage) o;
      int[] sorted = sortedMessage.result;
      printArray(sorted);
    }
  }
}

class StartActor extends UntypedActor {
  public void onReceive(Object o) throws Exception {
    if (o instanceof StartMessage) {
      ActorRef tester = getContext().actorOf(Props.create(TestActor.class), "Tester");
      ActorRef sorter = getContext().actorOf(Props.create(SorterActor.class), "SorterActor");
      tester.tell(new CallerMessage(sorter), ActorRef.noSender());
    }
  }
}
