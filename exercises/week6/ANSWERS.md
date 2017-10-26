# Exercise 6.1
## 1
 *Implement method V get(K k) using lock striping. It is similar to containsKey, but returns the value associated with key k if it is in the map, otherwise null. It should use the ItemNode.search auxiliary method.*

**Implemented: TestStripedMap.java**

## 2
*Implement method int size() using lock striping; it should return the total number of entries in the hash map. The size of stripe s is maintained in sizes[s], so the size() method could simply compute the sum of these values, locking each stripe in turn before accessing its value. Explain why it is important to lock stripe s when reading its size from sizes[s].*

**Implemented: TestStripedMap.java**

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
*Whatadvantagesaretheretousingasmallnumber(say32or16)ofstripesinsteadofsimplyhavingastripe for each entry in the buckets table? Discuss*

## 10
*Why can using 32 stripes improve performance even if one never runs more than, say, 16 threads? Discuss.*

## 11
*Acommentintheexamplesourcecodesaysthatitisimportantforthread-safetyofStripedMapandStriped- WriteMap that the number of buckets is a multiple of the number of stripes.
Give a scenario that demonstrates the lack of thread-safety when the number of buckets is not a multiple of the number of stripes. For instance, use 3 buckets and 2 stripes, and consider two concurrent calls put(k1,v1) and put(k2,v2) where the hash code of k1 is 5 and the hashcode of k2 is 8.*

