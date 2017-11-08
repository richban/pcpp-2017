// For week 6 -- four incomplete implementations of concurrent hash maps
// sestoft@itu.dk * 2014-10-07, 2015-09-25

// Parts of the code are missing.  Your task in the exercises is to
// write the missing parts.

import java.util.Random;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.function.IntToDoubleFunction;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class TestStripedWriteMap {
    public static final int OPERATIONS_PER_THREAD = 100;

  public static void main(String[] args) {
    seqTest(new StripedWriteMap<Integer, String>(77, 7));    // Must be run with: java -ea TestStripedMap

    switch (args[0].toLowerCase()) {
        case "homebrew":
            parallelTest(new StripedWriteMap<Integer, String>(77, 7));
            break;

        case "built-in":
            parallelTest(new WrapConcurrentHashMap<Integer, String>());
            break;
    }
  }

  // Very basic sequential functional test of a hash map.  You must
  // run with assertions enabled for this to work, as in
  //   java -ea TestStripedMap
  private static void seqTest(final OurMap<Integer, String> map) {
    System.out.printf("Sequential test %n%s%n", map.getClass());

    /*
        sizes private member is correctly initialized to 0
    */
    assert map.size() == 0;

    /*
        containsKey() on empty stripe size (1st && operand)
    */
    assert !map.containsKey(117);
    assert !map.containsKey(-2);

    /*
        get() on empty stripe size (1st || operand)
    */
    assert map.get(117) == null;

    /*
        put() actually added a pair. Tests that
        ItemNode.delete() does not update the Holder
        when the node is not found
    */
    assert map.put(117, "A") == null;

    /*
        containsKey() on existing pair, and
        implicitly non-zero stripe size (2nd && operand).
        Also tests that put() actually added the element.
    */
    assert map.containsKey(117);

    /*
        get() on existing element, and implicitly
        non-zero stripe size (2nd || operand).
    */
    assert map.get(117).equals("A");

    /*
        stripe size is updated correctly when
        adding new pairs. (ternary operator then branch)
    */
    assert map.put(17, "B") == null;
    assert map.size() == 2;

    /*
        containsKey() and get() reflect
        the update. Also, the two get()
        didn't override each other due
        to the key being different.
    */
    assert map.containsKey(17);
    assert map.get(117).equals("A");
    assert map.get(17).equals("B");

    /*
        put() on existing pair, the return value is actually
        the replaced one. Tests that ItemNode.delete() updates
        the Holder when the node is found.
    */
    assert map.put(117, "C").equals("A");

    /*
        The key was added back by put() after deleting it.
    */
    assert map.containsKey(117);

    /*
        get() reflects the update
    */
    assert map.get(117).equals("C");

    /*
        size() has not changed, since no
        pair was added. (ternary operator else branch)
    */
    assert map.size() == 2;

    /*
        forEach() loops though the whole map.
    */
    map.forEach((k, v) -> System.out.printf("%10d maps to %s%n", k, v));

    /*
        remove() on existing elements
    */
    assert map.remove(117).equals("C");

    /*
        The pair should be effectively removed
    */
    assert !map.containsKey(117);
    assert map.get(117) == null;
    assert map.size() == 1;

    /*
        ADDITIONAL TEST BY GROUP H
        remove() on non existing elements
    */
    assert map.remove(42) == null;

    /*
        ADDITIONAL TEST BY GROUP H
        The map size should be unchanges
    */
    assert map.size() == 1;


    /*
        putIfAbsent() on existing pair (if statement then branch)
    */
    assert map.putIfAbsent(17, "D").equals("B");

    /*
        map unchanged by putIfAbsent()
    */
    assert map.get(17).equals("B");
    assert map.size() == 1;
    assert map.containsKey(17);

    /*
        putIfAbsent() on non-found pair (if statement else branch)
    */
    assert map.putIfAbsent(217, "E") == null;

    /*
        map updated accordingly
    */
    assert map.get(217).equals("E");
    assert map.size() == 2;
    assert map.containsKey(217);

    assert map.putIfAbsent(34, "F") == null;
    map.forEach((k, v) -> System.out.printf("%10d maps to %s%n", k, v));

    /*
        The following is testing for reallocateBuckets(),
        ehose body in empty in StripedWriteMap. It is then
        not meaningful in this context, other than confirming
        that a function with an empty body does not harm
        our data strucutre.
    */
    map.reallocateBuckets();
    assert map.size() == 3;
    assert map.get(17).equals("B") && map.containsKey(17);
    assert map.get(217).equals("E") && map.containsKey(217);
    assert map.get(34).equals("F") && map.containsKey(34);
    map.forEach((k, v) -> System.out.printf("%10d maps to %s%n", k, v));
    map.reallocateBuckets();
    assert map.size() == 3;
    assert map.get(17).equals("B") && map.containsKey(17);
    assert map.get(217).equals("E") && map.containsKey(217);
    assert map.get(34).equals("F") && map.containsKey(34);
    map.forEach((k, v) -> System.out.printf("%10d maps to %s%n", k, v));
  }

  public static void parallelTest(final OurMap<Integer, String> map) {
      System.out.printf("Parallel test %n%s%n", map.getClass());

      int threadsCount = Runtime.getRuntime().availableProcessors() * 4;
      ExecutorService executor = Executors.newWorkStealingPool(threadsCount);
      Collection<Future<Long>> futures = new ArrayList<>(threadsCount);

      CyclicBarrier barrier = new CyclicBarrier(threadsCount);
      for (int k = 0; k < threadsCount; ++k) {
		  final int index = k;
          futures.add(executor.submit(() -> {
              long keysSum = 0;
              String value = Thread.currentThread().getName();
              long seed = System.currentTimeMillis() + value.hashCode();
              IntStream randomInts = new Random(seed).ints(OPERATIONS_PER_THREAD);
              barrier.await();
              Iterator<Integer> iterator = randomInts.iterator();

              while (iterator.hasNext()) {
                  int key = iterator.next();

                  switch (key % 4) {
                      case 0:
                        map.containsKey(key);
                        break;

                      case 1:
                        keysSum += map.put(key, index + ":" + key) == null ? key : 0;
                        break;

                        case 2:
                          keysSum += map.putIfAbsent(key, index + ":" + key) == null ? key : 0;
                          break;

                        case 3:
                            keysSum -= map.remove(key) == null ? 0 : key;
                            break;
                  }
              }

              return keysSum;
          }));
      }

      long threadsKeysSum = 0;
      for (Future<Long> fut : futures) {
          try {
                threadsKeysSum += fut.get();
          }
          catch (InterruptedException | ExecutionException e) {
              System.err.println("A thread was interrupted/had an error...");
          }
      }

      // Used due to final costraints on lambdas
      final LongStream.Builder mapKeys = LongStream.builder();
      map.forEach((key, value) -> {
		  assert IntStream.range(0, threadsCount).anyMatch((k) -> value.equals(k + ":" + key));
          mapKeys.add(key);
      });

      assert threadsKeysSum == mapKeys.build().sum();
  }
}

interface Consumer<K,V> {
  void accept(K k, V v);
}

interface OurMap<K,V> {
  boolean containsKey(K k);
  V get(K k);
  V put(K k, V v);
  V putIfAbsent(K k, V v);
  V remove(K k);
  int size();
  void forEach(Consumer<K,V> consumer);
  void reallocateBuckets();
}

// ----------------------------------------------------------------------
// A hashmap that permits thread-safe concurrent operations, using
// lock striping (intrinsic locks on Objects created for the purpose),
// and with immutable ItemNodes, so that reads do not need to lock at
// all, only need visibility of writes, which is ensured through the
// AtomicIntegerArray called sizes.

// NOT IMPLEMENTED: get, putIfAbsent, size, remove and forEach.

// The bucketCount must be a multiple of the number lockCount of
// stripes, so that h % lockCount == (h % bucketCount) % lockCount and
// so that h % lockCount is invariant under doubling the number of
// buckets in method reallocateBuckets.  Otherwise there is a risk of
// locking a stripe, only to have the relevant entry moved to a
// different stripe by an intervening call to reallocateBuckets.

class StripedWriteMap<K,V> implements OurMap<K,V> {
  // Synchronization policy: writing to
  //   buckets[hash] is guarded by locks[hash % lockCount]
  //   sizes[stripe] is guarded by locks[stripe]
  // Visibility of writes to reads is ensured by writes writing to
  // the stripe's size component (even if size does not change) and
  // reads reading from the stripe's size component.
  private volatile ItemNode<K,V>[] buckets;
  private final int lockCount;
  private final Object[] locks;
  private final AtomicIntegerArray sizes;

  public StripedWriteMap(int bucketCount, int lockCount) {
    if (bucketCount % lockCount != 0)
      throw new RuntimeException("bucket count must be a multiple of stripe count");
    this.lockCount = lockCount;
    this.buckets = makeBuckets(bucketCount);
    this.locks = new Object[lockCount];
    this.sizes = new AtomicIntegerArray(lockCount);
    for (int stripe=0; stripe<lockCount; stripe++)
      this.locks[stripe] = new Object();
  }

  @SuppressWarnings("unchecked")
  private static <K,V> ItemNode<K,V>[] makeBuckets(int size) {
    // Java's @$#@?!! type system requires "unsafe" cast here:
    return (ItemNode<K,V>[])new ItemNode[size];
  }

  // Protect against poor hash functions and make non-negative
  private static <K> int getHash(K k) {
    final int kh = k.hashCode();
    return (kh ^ (kh >>> 16)) & 0x7FFFFFFF;
  }

  // Return true if key k is in map, else false
  public boolean containsKey(K k) {
    final ItemNode<K,V>[] bs = buckets;
    final int h = getHash(k), stripe = h % lockCount, hash = h % bs.length;
    // The sizes access is necessary for visibility of bs elements
    return sizes.get(stripe) != 0 && ItemNode.search(bs[hash], k, null);
  }

  // Return value v associated with key k, or null
  public V get(K k) {
    // TO DO: IMPLEMENT
     final ItemNode<K,V>[] bs = buckets;
    final int h = getHash(k), stripe = h % lockCount, hash = h % bs.length;
    // The sizes access is necessary for visibility of bs elements
	Holder<V> holder = new Holder<V>();
	boolean found = ItemNode.search(bs[hash], k, holder);
    if (sizes.get(stripe) == 0 || !found) return null;
	return holder.value;
  }

  public int size() {
    // TO DO: IMPLEMENT
    int count = 0;
    for (int i = 0; i < lockCount; i++)
			count+= sizes.get(i);
	return count;
  }

  // Put v at key k, or update if already present.  The logic here has
  // become more contorted because we must not hold the stripe lock
  // when calling reallocateBuckets, otherwise there will be deadlock
  // when two threads working on different stripes try to reallocate
  // at the same time.
  public V put(K k, V v) {
    final int h = getHash(k), stripe = h % lockCount;
    final Holder<V> old = new Holder<V>();
    ItemNode<K,V>[] bs;
    int afterSize;
    synchronized (locks[stripe]) {
      bs = buckets;
      final int hash = h % bs.length;
      final ItemNode<K,V> node = bs[hash],
        newNode = ItemNode.delete(node, k, old);
      bs[hash] = new ItemNode<K,V>(k, v, newNode);
      // Write for visibility; increment if k was not already in map
      afterSize = sizes.addAndGet(stripe, newNode == node ? 1 : 0);
    }
    if (afterSize * lockCount > bs.length)
      reallocateBuckets(bs);
    return old.get();
  }

  // Put v at key k only if absent.
  public V putIfAbsent(K k, V v) {
      // TO DO: IMPLEMENT
      final int h = getHash(k), stripe = h % lockCount;
          final int hash = h % buckets.length;
	      Holder<V> holder = new Holder<V>();
          synchronized (locks[stripe]) {
              if (ItemNode.search(buckets[hash], k,holder)) {
                  return holder.value;
              }
          }
	      return put(k,v);
  }

  // Remove and return the value at key k if any, else return null
  public V remove(K k) {
    // TO DO: IMPLEMENT
    final int h = getHash(k), stripe = h % lockCount;
    synchronized (locks[stripe]) {
      final int hash = h % buckets.length;

	  Holder<V> holder = new Holder<V>();
      buckets[hash] = ItemNode.delete(buckets[hash], k, holder);
      sizes.addAndGet(stripe, holder.value == null ? 0 : -1);
      return holder.value;
    }
  }

  // Iterate over the hashmap's entries one stripe at a time.
  public void forEach(Consumer<K,V> consumer) {
    // TO DO: IMPLEMENT
	ItemNode<K,V>[] bs = buckets;
	for (int i = 0; i < lockCount; i++)
		sizes.get(i);
	for (int i = 0; i < bs.length; i++)
	{
				ItemNode<K,V> node = bs[i];
				while (node != null)
				{
					consumer.accept(node.k,node.v);
					node = node.next;
				}
	}
  }

  // Now that reallocation happens internally, do not do it externally
  public void reallocateBuckets() { }

  // First lock all stripes.  Then double bucket table size, rehash,
  // and redistribute entries.  Since the number of stripes does not
  // change, and since buckets.length is a multiple of lockCount, a
  // key that belongs to stripe s because (getHash(k) % N) %
  // lockCount == s will continue to belong to stripe s.  Hence the
  // sizes array need not be recomputed.

  // In any case, do not reallocate if the buckets field was updated
  // since the need for reallocation was discovered; this means that
  // another thread has already reallocated.  This happens very often
  // with 16 threads and a largish buckets table, size > 10,000.

  public void reallocateBuckets(final ItemNode<K,V>[] oldBuckets) {
    lockAllAndThen(() -> {
        final ItemNode<K,V>[] bs = buckets;
        if (oldBuckets == bs) {
          // System.out.printf("Reallocating from %d buckets%n", bs.length);
          final ItemNode<K,V>[] newBuckets = makeBuckets(2 * bs.length);
          for (int hash=0; hash<bs.length; hash++) {
            ItemNode<K,V> node = bs[hash];
            while (node != null) {
              final int newHash = getHash(node.k) % newBuckets.length;
              newBuckets[newHash]
                = new ItemNode<K,V>(node.k, node.v, newBuckets[newHash]);
              node = node.next;
            }
          }
          buckets = newBuckets; // Visibility: buckets field is volatile
        }
      });
  }

  // Lock all stripes, perform action, then unlock all stripes
  private void lockAllAndThen(Runnable action) {
    lockAllAndThen(0, action);
  }

  private void lockAllAndThen(int nextStripe, Runnable action) {
    if (nextStripe >= lockCount)
      action.run();
    else
      synchronized (locks[nextStripe]) {
        lockAllAndThen(nextStripe + 1, action);
      }
  }

  static class ItemNode<K,V> {
    private final K k;
    private final V v;
    private final ItemNode<K,V> next;

    public ItemNode(K k, V v, ItemNode<K,V> next) {
      this.k = k;
      this.v = v;
      this.next = next;
    }

    // These work on immutable data only, no synchronization needed.

    public static <K,V> boolean search(ItemNode<K,V> node, K k, Holder<V> old) {
      while (node != null)
        if (k.equals(node.k)) {
          if (old != null)
            old.set(node.v);
          return true;
        } else
          node = node.next;
      return false;
    }

    public static <K,V> ItemNode<K,V> delete(ItemNode<K,V> node, K k, Holder<V> old) {
      if (node == null)
        return null;
      else if (k.equals(node.k)) {
        old.set(node.v);
        return node.next;
      } else {
        final ItemNode<K,V> newNode = delete(node.next, k, old);
        if (newNode == node.next)
          return node;
        else
          return new ItemNode<K,V>(node.k, node.v, newNode);
      }
    }
  }

  // Object to hold a "by reference" parameter.  For use only on a
  // single thread, so no need for "volatile" or synchronization.

  static class Holder<V> {
    private V value;
    public V get() {
      return value;
    }
    public void set(V value) {
      this.value = value;
    }
  }
}

// ----------------------------------------------------------------------
// A wrapper around the Java class library's sophisticated
// ConcurrentHashMap<K,V>, making it implement OurMap<K,V>

class WrapConcurrentHashMap<K,V> implements OurMap<K,V> {
  final ConcurrentHashMap<K,V> underlying = new ConcurrentHashMap<K,V>();

  public boolean containsKey(K k) {
    return underlying.containsKey(k);
  }

  public V get(K k) {
    return underlying.get(k);
  }

  public V put(K k, V v) {
    return underlying.put(k, v);
  }

  public V putIfAbsent(K k, V v) {
    return underlying.putIfAbsent(k, v);
  }

  public V remove(K k) {
    return underlying.remove(k);
  }

  public int size() {
    return underlying.size();
  }

  public void forEach(Consumer<K,V> consumer) {
    underlying.forEach((k,v) -> consumer.accept(k,v));
  }

  public void reallocateBuckets() { }
}
