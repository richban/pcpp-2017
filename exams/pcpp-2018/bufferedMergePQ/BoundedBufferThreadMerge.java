import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class BoundedBufferThreadMerge implements PQ, Runnable {
    int[] buffer;
    int first,last; // first points at the next one to take, last at the next to insert
    // equals means empty; wrap around 

    PQ left,right;

    boolean finished = false;

    Parameters param;
    
    static ArrayList<Thread> AllThreads = new ArrayList<Thread>();

    public void shutDown() {
        for(Thread t : AllThreads) t.stop();
        AllThreads = new ArrayList<Thread>();
    }
    public void start() {
        for(Thread t : AllThreads) t.start();
    }

    public BoundedBufferThreadMerge(Parameters pp) {
        param = pp;
        int bufLen = param.bufLen;
        PQPair pqPair = new SerialPQPair();
        // this is the recursion; look at SerialPQPair.java and Parameters.java (and PQPair.java) for details
        // Paramaters know how and how long to split recursively
        if( param.splitChildren() ) {
            pqPair.createPairParam(param,(p)-> new BoundedBufferThreadMerge(p));
        } else {
            pqPair.createPairParam(param,(p)-> new OneBufferPQ(p));
        }        
        left  = pqPair.getLeft();
        right = pqPair.getRight();
        buffer = new int[bufLen];
        first = last = 0;
        Thread me = new Thread(this);
        AllThreads.add(me);
        // me.start();  // this is now down in start()
    }
    public void run() {
        int nextL = left.getMin(), nextR=right.getMin();
        while( nextL != Integer.MAX_VALUE || nextR != Integer.MAX_VALUE ) {
            if(nextL < nextR) {
                insert(nextL);
                nextL=left.getMin();
            } else {
                insert(nextR);
                nextR=right.getMin();
            }
        }
        insert(Integer.MAX_VALUE);
    }
    
    public boolean isEmpty() { return first == last;}
    public boolean isFull() { return last+1 == first || last+1 - buffer.length == first;}

    public int peek() {
        //        throw(new RuntimeExcepetion ("not implemented"));
        System.err.println("not implemented");
        System.exit(2);
        return Integer.MAX_VALUE;
    }
    
    private void doInsert(int item) { // call only if not full
        assert(!isFull());
        buffer[last] = item;
        last ++;
        if(last >= buffer.length) last = last - buffer.length;
        assert(first != last);
    }
    public void insert(int item) {
            while( isFull() && ! finished ) {
            }
            doInsert(item);
    }
    private int doTake() { // call only if not empty
        assert(!isEmpty() );
        int res = buffer[first];
        if( res != Integer.MAX_VALUE ) {
            first = first+1;
            if(first >= buffer.length) first = first - buffer.length;
        }
        if(res == Integer.MAX_VALUE ) finished = true;
        return res;
    }
    public int getMin() {
        if(finished) return Integer.MAX_VALUE;
            while( isEmpty() ) {          
            }
            int r = doTake();
            return r;
    }
    public void print(int d) {
        String out=(param.treeposInd());
        if( !isEmpty() ) {
            if(first<last) {
                out=out+stringArray(buffer,first,last);
            } else {
                out="X"+out+stringArray(buffer,first,buffer.length)+"|"+stringArray(buffer,0,last);
            }
        }
        System.out.println("Y"+out);
        left.print(d+1);
        right.print(d+1);
        return;
    }
    
    
    static public void printArray(int[] arr, int f, int l) {
        if( arr != null) {
            for(int i=f; i< l;i++) System.out.print(" "+(arr[i] == Integer.MAX_VALUE ? ". " : ""+arr[i] ));
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
