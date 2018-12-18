import java.util.Arrays;

public class OneBufferPQ implements PQ {
    private final int[] buffer;
    private int current;

    Parameters param;
    OneBufferPQ(Parameters p ) {
        param = p;
        int [] inp = p.input;
        int l=p.left; int r=p.right; int buflen= p.bufLen; int d = p.depth;
        buffer = new int[1+r - l];
        for (int i = 0; i < r - l; i++) {
            buffer[i] = inp[l + i];
        }
        buffer[r-l] = Integer.MAX_VALUE;
        if(p.sortParallel) {
            Arrays.parallelSort(buffer);
        } else {
            Arrays.sort(buffer);
        }            
        current=0;
    }

    // See the top element of the buffer without popping it off the queue.
    public int peek() {
        return buffer[current];
    }

    // Checks if the PQ is empty.
    public boolean isEmpty() {
        return peek() == Integer.MAX_VALUE;
    }
    public int getMin() {
        int res = peek();
        if(current < buffer.length -1 ) current++;
        return res;
    }
    public void print(int d) {
        System.out.println( param.treeposInd() + stringArray(buffer,current,buffer.length) );
        return;
    }

    static private void printArray(int[] arr, int f) {
        if (arr != null) {
            for (int i = f; i < arr.length; i++) {
                System.out.print(" " + (arr[i] == Integer.MAX_VALUE ? ". " : "" + arr[i]));
            }
        }
    }
    static public String stringArray(int[] arr, int f, int l) {
        String res ="";
        if( arr != null) {
            for(int i=f; i< l;i++) res = res + (" "+(arr[i] == Integer.MAX_VALUE ? ". " : ""+arr[i] ));
        }
        return res;
    }

}
