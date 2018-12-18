public interface PQ {
    // check if the PQ is empty
    boolean isEmpty();

    // get the value of the minimum element without removing it
    int peek();

    // get and remove the minimum element.
    int getMin();

    // print the queue.
    void print(int depth);

    // needed for stopping threads in BoundedBufferThreadMerge
    default void shutDown() {
    };

    // needed for starting threads in BoundedBufferThreadMerge
    default void start() {
    };
}
