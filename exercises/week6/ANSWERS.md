Group: 23

*Name 1:* Patrick Evers Bjoerkman (pebj@itu.dk)
*Name 2:* Davide Laezza (dlae@itu.dk)
*Name 3:* Richard Banyi (riba@itu.dk)

# Exercise 6.1
## 1
 *Implement method V get(K k) using lock striping. It is similar to containsKey,
 but returns the value associated with key k if it is in the map, otherwise null.
 It should use the ItemNode.search auxiliary method.*

**Implemented: TestStripedMap.java**

## 2
*Implement method int size() using lock striping; it should return the total
number of entries in the hash map. The size of stripe s is maintained in sizes[s],
so the size() method could simply compute the sum of these values, locking each
stripe in turn before accessing its value. Explain why it is important to lock
stripe s when reading its size from sizes[s].*

**Implemented: TestStripedMap.java**

It's important because it improves the scalabillity of the implementation.
A stripe might be updated right after we read it's size, therefore we have to
hold the lock for while we read and write.
## 3
*Implement method V putIfAbsent(K k, V v) using lock striping. It must work as in
Java’s ConcurrentHashMap, that is, atomically do the following: check whether
key k is already in the map; if it is, return the associated value; and if it is
not, add (k,v) to the map and return null. The implementation can be similar to
putIfAbsent in class SynchronizedMap<K,V> but should of course only lock on the
stripe that will hold key k. It should use the ItemNode.search auxiliary method.
Remember to increment the relevant sizes[stripe] count if any entry was added.
Ignore reallocateBuckets for now*

**Implemented: TestStripedMap.java**

## 4
*Extend method putIfAbsent to call reallocateBuckets when the bucket lists grow
too long. For simplicity, you can test whether the size of a stripe is greater
than the number of buckets divided by the number of stripes, as in method put.*

**Not Implemented: TestStripedMap.java**


## 5
*Implement method V remove(K k) using lock striping. Again very similar to
SynchronizedMap<K,V>. Remember to decrement the relevant sizes[stripe] count if
any entry was removed.*

**Implemented: TestStripedMap.java**


## 6
*Implement method void forEach(Consumer<K,V> consumer). Apparently, this may be
implemented in two ways: either (1) iterate through the buckets as in the
SynchronizedMap<K,V> implementa- tion; or (2) iterate over the stripes, and for
each stripe iterate over the buckets that belong to that stripe.*

**Implemented: TestStripedMap.java**

## 7
*You may use method testMap(map) for very basic single-threaded functional
testing while making the above method implementations. See how to call it in
method testAllMaps. To actually enable the assert statements, run with the -ea
option:*

**All test passed**

## 8
*Measure the performance of SynchronizedMap<K,V> and StripedMap<K,V> by timing
calls to method exerciseMap. Report the results from your hardware and discuss
whether they are as expected.*

```bash
SynchronizedMap       16         526372.9 us   30438.09          2
99992.0
StripedMap            16          89021.8 us    2626.82          4
99992.0
StripedWriteMap       16          39715.6 us    1923.66          8
0.0
WrapConcHashMap       16          45341.3 us    1478.50          8
99992.0
```

`SynchronizedMap` appears to be almost six times slower than `StripedMap`: this
can be interpreted as a confirmation that lock contention is a great source of
sequentiality in parallel computations. Some synchronization holdup are still
present though, since an execution speed increased by a factor of six on a 16
threaded machine is synonymous with incomplete parallelism. Such delays are
basically solved in `StripedWriteMap`, which runs 13 times faster than
`SynchronizedMap`, which is near enough to the theoretical speed boost by a
factor of 16. `WrapConcHashMap` performs slightly worse than the precedent,
probably because of advanced implementation machinery that allow even higher
scalability.

## 9
*What advantages are there to using a small number (say 32 or 16) of stripes
instead of simply having a stripe for each entry in the buckets table? Discuss*

A separate lock for every bucket could be implemented by either having an
array of lock `Object`s as large as the bucket, or by locking on the bucked
itself.

In the first case, the size of the lock array should be kept consistent
with the number of buckets, increasing the complexity of the task, in addition
to a bigger memory footprint.

The second case implies that the bucket references are final, making impossible
any implementation of `StripedWriteMap`, reducing the potential concurrency
in the data structure.

## 10
*Why can using 32 stripes improve performance even if one never runs more than,
say, 16 threads? Discuss.*

The stripes that are actually used are not related in any way to the number of
threads: indeed, the lock used when accessing the data structure depends on the
key alone. A higher number of stripes means less chances of two or more threads
trying to acquire the same lock.

## 11
*A comment in the example source code says that it is important for thread-safety
of StripedMap and StripedWriteMap that the number of buckets is a multiple of the
number of stripes. Give a scenario that demonstrates the lack of thread-safety when
the number of buckets is not a multiple of the number of stripes. For instance,
use 3 buckets and 2 stripes, and consider two concurrent calls put(k1,v1) and
put(k2,v2) where the hash code of k1 is 5 and the hashcode of k2 is 8.*

The problem arises in every case when two hashes compute to the same bucket but
different stripe index: in such situation, more than one lock is used to guard
the same concurrency-sensitive resource, which will probably lead to concurrent
execution of mutually exclusive sections of code.


# Exercise 6.2

## Task 1-2

**Source file: TestStripedMap.java**

## Task 3
*Why do you not need to write to the stripe size if nothing was added?
If nothing is added, then the size does not change hence nothing is written to stripe size.

## Task 4
**Implemented: TestStripedMap.java**

## Task 5 
**Implemented: TestStripedMap.java**


## Task 6
*Measure the performance of SynchronizedMap<K,V>, StripedMap<K,V> StripedWriteMap<K,V> and
WrapConcurrentHashMap<K,V> using method exerciseAllMaps. Report the results and discuss whether
they are as expected.*

SynchronizedMap       16         535909,0 us   10741,82          2
99992.0
StripedMap            16         205976,7 us   33256,60          2
99992.0
StripedWriteMap       16         141977,9 us   16738,77          2
99992.0
WrapConcHashMap       16         155192,3 us   35772,82          2

SynchronizedMap is not scalable by the number of threads, rather it is only thread-safe. All its methods lock on the same object and as such, no concurrency is being
utilized between the threads. It is therefore expected that this one has the highest runningtime. StripedMap uses a number of locks for its different buckets such that
if a bucket is locked, another thread is capable of working on another bucket in parralel. StripedWriteMap (unlike StripedMap) does not use locking when accessing data,
thus it is expected that StripedWriteMap is faster than StripedMap. WrapConcHashMap utilizes a java library and may utilize a number of techniques aside from
StripedWriteMap.

# Exercise 6.3

## Task 1

*Report the numbers and discuss whether they are plausible*

`AtomicLong` is by far the slowest class, being roughly 4 times slower than
`NewLongAdder` the second slowest one. This is consistent with Java 8 documentation,
for `LongAdder`, which states that `AtomicLong` is less suited that the latter
for high lock-contention scenarios. It also shows the smallest standard deviation,
which means that it is a poor choice in high-concurrency situations.
`NewLongAdder`, despite trying to mimic the built-in `LongAdder`, falls short
behind `LongCounter`, which emulates `AtomicLong` instead. This is probably due
to the fact that it accomplishes a more complex task, which is more efficiently
handled at a lower level than Java application code. Nevertheless, it is still
four times faster than `AtomicLong`, consistently with the four-threaded CPU the
tests were run on: this confirms that lock contention is a significant issue,
even for seemingly low-concurrency situations.
Following closely, we have `LongCounter`: its simple task and its small memory
footprint are probably the reasons for which it does not have terrible performances
despite its naive implementation. However, it must be pointed out that the
standard deviation is impressively high, accounting for almost half of the average
and being six times `NewLongAdder` one's, the second greatest. Hence, the
measurements for this class are not really meaningful, as it tends to be for
simple custom classes which handle naively a library task, and are, of course,
not as well tested and engineered.
Then `NewLongAdderPadded` confirms that the weird allocation strategy works for
average multi-core Intel processors: on a dual-core hyperthreaded i5 CPU it
carries out the task in less than half of the time than its non-padded counterpart.
In the fastest position there is the `LongAdder` built-in class: not only it is
part of the standard library, thus having access to lower-level facilities, but
it is also the most suited class for our high-concurrency task.

Below, the output of three different runs of the tests:
```
OS:   Linux; 4.13.0-1-amd64; amd64
JVM:  Oracle Corporation; 1.8.0_151
CPU:  null; 4 "cores"
Date: 2017-10-26T19:50:00+0200

current thread hashCode               0.0 us       0.00  134217728
ThreadLocalRandom                     0.0 us       0.00   67108864
current thread hashCode               0.0 us       0.00  134217728
ThreadLocalRandom                     0.0 us       0.00   67108864
AtomicLong                      1302775.9 us   18295.68          2
LongAdder                        157180.7 us    5133.70          2
LongCounter                      462705.3 us  291588.83          2
NewLongAdder                     475875.8 us   59364.18          2
NewLongAdderPadded               194537.9 us   23966.31          2
```
```bash
OS:   Linux; 4.13.0-1-amd64; amd64
JVM:  Oracle Corporation; 1.8.0_151
CPU:  null; 4 "cores"
Date: 2017-10-26T19:50:00+0200

current thread hashCode               0.0 us       0.00  134217728
ThreadLocalRandom                     0.0 us       0.00   67108864
AtomicLong                      1303676.6 us   11192.57          2
LongAdder                        168535.8 us   33160.23          2
LongCounter                      451518.1 us  282737.01          2
NewLongAdder                     489303.2 us   45384.73          2
NewLongAdderPadded               191695.1 us   13962.27          2
```
```bash
OS:   Linux; 4.13.0-1-amd64; amd64
JVM:  Oracle Corporation; 1.8.0_151
CPU:  null; 4 "cores"
Date: 2017-10-26T19:50:00+0200
current thread hashCode               0.0 us       0.00  134217728
ThreadLocalRandom                     0.0 us       0.00   67108864
AtomicLong                      1304980.8 us   17323.54          2
LongAdder                        156200.1 us    4268.10          2
LongCounter                      439038.7 us  214808.67          2
NewLongAdder                     476546.6 us   54344.08          2
NewLongAdderPadded               191994.3 us   17160.81          2
```
## Task 2
*Do those new Object() allocations make any difference, positive or negative, on your version of the
Java Virtual Machine, and your hardware?*

As stated in the comments in the source code, the CPU the tests are run on is
in the family targeted by that weird optimization, which means that the `Object`
allocations lead to improved performances. The results are also consistent with
those of `NewLongAdder`, which is slower due to the usage of the higher-level API
`AtomicLongArray` instead of a lower-level facility such as a native array.
Following, results of three different experiments run:

```bash
OS:   Linux; 4.13.0-1-amd64; amd64
JVM:  Oracle Corporation; 1.8.0_151
CPU:  null; 4 "cores"
Date: 2017-10-26T22:15:07+0200
NewLongAdderLessPadded           248906.8 us   17014.90          2
```
```bash
OS:   Linux; 4.13.0-1-amd64; amd64
JVM:  Oracle Corporation; 1.8.0_151
CPU:  null; 4 "cores"
Date: 2017-10-26T22:15:13+0200
NewLongAdderLessPadded           241544.5 us   21237.45          2
```
```bash
OS:   Linux; 4.13.0-1-amd64; amd64
JVM:  Oracle Corporation; 1.8.0_151
CPU:  null; 4 "cores"
Date: 2017-10-26T22:15:18+0200
NewLongAdderLessPadded           249881.9 us   13006.70          2
```
