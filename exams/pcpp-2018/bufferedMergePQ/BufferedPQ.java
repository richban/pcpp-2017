import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

// A PQ class which is defined by a buffer of elements and two recursive PQs
// If the buffer gets empty, refill it by merging from the recursive PQs
// Integer.MAX_VALUE denotes the end of the PQ

// measured in Runner.java

public class BufferedPQ implements PQ {
    // buffer containing the elements in order
    private int[] buffer;
    // the two recursive priority queues, which will store elements which cannot
    // stay in "buffer"
    PQ left = null, right = null;

    int bufLen;
    int current;
    
    // Constructor which creates two recursive Priority queues, and fills up the
    // buffer for a first time.
    public Parameters param;
    BufferedPQ(Parameters pp) {
        param = pp;
        bufLen = param.bufLen;
        PQPair pqPair = param.pqPair.clone();
        // this is the recursion; look at SerialPQPair.java and Parameters.java (and PQPair.java) for details
        // Paramaters knows if to split recursively, pqPair creates the two recursive instances
        if( param.splitChildren() ) {
            pqPair.createPairParam(param,(p)-> new BufferedPQ(p));
        } else {
            pqPair.createPairParam(param,(p)-> new OneBufferPQ(p));
        }        
        left  = pqPair.getLeft();
        right = pqPair.getRight();
        buffer = getNewBuffer(bufLen);
        current=0;
    }

    // Gets the mininmal element. If all the elements of the buffer are taken (that is
    // current >= buffer.length), then we need to set the buffer to be the
    // nextBuffer, and then refill nextBuffer from the two recursive PQ.
    public int getMin() {
        int res = peek();
        current++;
        if (current >= buffer.length) { // the buffer is empty
            buffer = getNewBuffer(bufLen);
            current = 0;
        }
        return res;
    }

    // See the top element of the buffer without popping it off the queue.
    public int peek() {
        return buffer[current];
    }

    // Checks if the PQ is empty.
    public boolean isEmpty() {
        return peek() == Integer.MAX_VALUE;
    }

    // Fills the buffer with elements from the two recursive PQs, making sure
    // that elements are taken in order.
    private int[] getNewBuffer(int len) {
        int[] res = new int[len];
        for (int i = 0; i < len; i++) {
            // Add to position i the smallest element between the minimum element
            // of the left queue and the minimum element of the right queue.
            res[i] = left.peek() < right.peek() ? left.getMin() : right.getMin();
        }
        return res;
    }

    // A recursive print method for seeing the queue.
    public void print(int d) {
        String out=(param.treeposInd());
        if( true ) { //!isEmpty() ) {
            out=out+stringArray(buffer,current,buffer.length);
        }
        System.out.println(out); // + " L"+ left.param+" R"+right.param);
        left.print(d+1);
        right.print(d+1);
        return;
    }

    static public String stringArray(int[] arr, int f, int l) {
        String res ="";
        if( arr != null) {
            for(int i=f; i< l;i++) res = res + (" "+(arr[i] == Integer.MAX_VALUE ? ". " : ""+arr[i] ));
        }
        return res;
    }

    // prints arr elements from f to arr.length
    static private void printArray(int[] arr, int f) {
        if (arr != null) {
            for (int i = f; i < arr.length; i++) {
                System.out.print(" " + (arr[i] == Integer.MAX_VALUE ? ". " : "" + arr[i]));
            }
        }
    }

}
