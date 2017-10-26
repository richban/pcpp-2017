# Exercise 6.1
## 1
 *Implement method V get(K k) using lock striping. It is similar to containsKey, but returns the value associated with key k if it is in the map, otherwise null. It should use the ItemNode.search auxiliary method.*

**Implemented: TestStripedMap.java**

## 2
*Implement method int size() using lock striping; it should return the total number of entries in the hash map. The size of stripe s is maintained in sizes[s], so the size() method could simply compute the sum of these values, locking each stripe in turn before accessing its value. Explain why it is important to lock stripe s when reading its size from sizes[s].*

**Implemented: TestStripedMap.java**

It's important because it improves the scalabillity of the implementation.
A stripe might be updated right after we read it's size, therefore we have to
hold the lock for while we read and write.
## 3
*Implement method V putIfAbsent(K k, V v) using lock striping. It must work as in Java’s Concur- rentHashMap, that is, atomically do the following: check whether key k is already in the map; if it is, return the associated value; and if it is not, add (k,v) to the map and return null. The implementation can be similar to putIfAbsent in class SynchronizedMap<K,V> but should of course only lock on the stripe that will hold key k. It should use the ItemNode.search auxiliary method. Remember to increment the relevant sizes[stripe] count if any entry was added. Ignore reallocateBuckets for now*

**Implemented: TestStripedMap.java**

## 4
*Extend method putIfAbsent to call reallocateBuckets when the bucket lists grow too long. For simplicity, you can test whether the size of a stripe is greater than the number of buckets divided by the number of stripes, as in method put.*

**Not Implemented: TestStripedMap.java**


## 5
*Implement method V remove(K k) using lock striping. Again very similar to SynchronizedMap<K,V>. Remember to decrement the relevant sizes[stripe] count if any entry was removed.*

**Implemented: TestStripedMap.java**


## 6
*Implement method void forEach(Consumer<K,V> consumer). Apparently, this may be imple- mented in two ways: either (1) iterate through the buckets as in the SynchronizedMap<K,V> implementa- tion; or (2) iterate over the stripes, and for each stripe iterate over the buckets that belong to that stripe.*

**Implemented: TestStripedMap.java**

## 7
*You may use method testMap(map) for very basic single-threaded functional testing while making the above method implementations. See how to call it in method testAllMaps. To actually enable the assert statements, run with the -ea option:*

**All test passed**

## 8
*Measure the performance of SynchronizedMap<K,V> and StripedMap<K,V> by timing calls to method exerciseMap. Report the results from your hardware and discuss whether they are as expected.*

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

## 9
*What advantages are there to using a small number (say 32 or 16) of stripes instead of simply having a stripe for each entry in the buckets table? Discuss*

## 10
*Why can using 32 stripes improve performance even if one never runs more than, say, 16 threads? Discuss.*

## 11
*Acommentintheexamplesourcecodesaysthatitisimportantforthread-safetyofStripedMapandStriped- WriteMap that the number of buckets is a multiple of the number of stripes.
Give a scenario that demonstrates the lack of thread-safety when the number of buckets is not a multiple of the number of stripes. For instance, use 3 buckets and 2 stripes, and consider two concurrent calls put(k1,v1) and put(k2,v2) where the hash code of k1 is 5 and the hashcode of k2 is 8.*

# Exercise 6.3
## Task 1


# OS:   Linux; 4.13.0-1-amd64; amd64
# JVM:  Oracle Corporation; 1.8.0_151
# CPU:  null; 4 "cores"
# Date: 2017-10-26T19:50:00+0200
current thread hashCode               0.0 us       0.00  134217728
ThreadLocalRandom                     0.0 us       0.00   67108864
AtomicLong                      1309661.4 us   10889.99          2
LongAdder                        161238.2 us   19707.93          2
LongCounter                      447485.0 us  258066.75          2
NewLongAdder                     476577.0 us   44694.15          2
NewLongAdderPadded               294118.2 us   20283.12          2
current thread hashCode               0.0 us       0.00  134217728
ThreadLocalRandom                     0.0 us       0.00   67108864
current thread hashCode               0.0 us       0.00  134217728
ThreadLocalRandom                     0.0 us       0.00   67108864
AtomicLong                      1302775.9 us   18295.68          2
LongAdder                        157180.7 us    5133.70          2
LongCounter                      462705.3 us  291588.83          2
NewLongAdder                     475875.8 us   59364.18          2
NewLongAdderPadded               194537.9 us   23966.31          2
current thread hashCode               0.0 us       0.00  134217728
ThreadLocalRandom                     0.0 us       0.00   67108864
AtomicLong                      1293032.8 us   27999.85          2
LongAdder                        157908.5 us    6405.79          2
LongCounter                      458055.8 us  283810.03          2
NewLongAdder                     469050.1 us   74704.29          2
NewLongAdderPadded               189231.0 us   13581.51          2
current thread hashCode               0.0 us       0.00  134217728
ThreadLocalRandom                     0.0 us       0.00   67108864
AtomicLong                      1303676.6 us   11192.57          2
LongAdder                        168535.8 us   33160.23          2
LongCounter                      451518.1 us  282737.01          2
NewLongAdder                     489303.2 us   45384.73          2
NewLongAdderPadded               191695.1 us   13962.27          2
current thread hashCode               0.0 us       0.00  134217728
ThreadLocalRandom                     0.0 us       0.00   67108864
AtomicLong                      1304980.8 us   17323.54          2
LongAdder                        156200.1 us    4268.10          2
LongCounter                      439038.7 us  214808.67          2
NewLongAdder                     476546.6 us   54344.08          2
NewLongAdderPadded               191994.3 us   17160.81          2
current thread hashCode               0.0 us       0.00  134217728
ThreadLocalRandom                     0.0 us       0.00   67108864
AtomicLong                      1306937.3 us   20512.81          2
LongAdder                        168210.8 us   45923.03          2
LongCounter                      467858.2 us  305831.28          2
NewLongAdder                     493132.9 us   58445.21          2
NewLongAdderPadded               189068.7 us   15302.87          2
