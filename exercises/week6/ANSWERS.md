Group: 23
*Name 1: Patrick Evers Bjoerkman (pebj@itu.dk)
*Name 2: Davide Laezza (dlae@itu.dk)
*Name 3: Richard Banyi (riba@itu.dk)

# Exercise 6.2

## Task 1-2
*** Source file: TestStripedMap.java ***


## Task 3
* Why do you not need to write to the stripe size if nothing was added?
 If no nodes need to be added then the stripe size will remain unchanged.

## Task 4-5
*** Source file: TestStripedMap.java ***


## Task 6
*Measure the performance of SynchronizedMap<K,V>, StripedMap<K,V> StripedWriteMap<K,V> and
WrapConcurrentHashMap<K,V> using method exerciseAllMaps. Report the results and discuss whether
they are as expected.

# OS:   Windows 10; 10.0; amd64
# JVM:  Oracle Corporation; 1.8.0_144
# CPU:  Intel64 Family 6 Model 78 Stepping 3, GenuineIntel; 4 "cores"
# Date: 2017-10-26T22:28:58+0200

class SynchronizedMap

class StripedMap

class StripedWriteMap

class WrapConcurrentHashMap
SynchronizedMap       16         565560,6 us   45216,87          2
99992.0
StripedMap            16         272790,7 us   66559,55          2
99992.0
StripedWriteMap       16         112430,8 us    6669,65          4
0.0
WrapConcHashMap       16         178763,1 us   61361,48          2
99992.0

SynchronizedMap is not scalable by the number of threads, rather it is only thread-safe. All its methods lock on the same object and as such, no concurrency is being
utilized between the threads. It is therefore expected that this one has the highest runningtime. StripedMap uses a number of locks for its different buckets such that
if a bucket is locked, another thread is capable of working on another bucket in parralel. StripedWriteMap (unlike StripedMap) does not use locking when accessing data,
thus it is expected that StripedWriteMap is faster than StripedMap. WrapConcHashMap utilizes a java library and may utilize a number of techniques aside from 
StripedWriteMap. 