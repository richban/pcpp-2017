# Exercise 2.1
## 1
*Write a sequential program to compute the total number of prime factors of the
integers in range 0 to 4,999,999. The result should be 18,703,729. How much time
does this take?*

**Source file: SerialFactorsCounter.java**
The Unix `time` output for the program is:

```bash
real    0m7.130s
user    0m7.131s
sys     0m0.008s
```
In addition to showing that my laptop is clearly not a computing powerhouse, the
output confirms that no parallelism is happening.

## 2
*Write such a MyAtomicInteger class.*

**Source file: MyAtomicInteger.java**

## 3
*Write a parallel program that uses 10 threads to count the total number of prime
factors of the integers in range 0 to 4,999,999. [...] Do you still get the
correct answer? How much time does this take?*

**Source file: ParallelFactorsCounter.java**

The answer is the same as in the seial case, that is 18703729. The `time`
information, however, differs significantly:

```bash
real    0m3.617s
user    0m13.535s
sys     0m0.020s
```
On my dual-core, hyper threaded CPU the wall time is approximately one fourth of
the CPU time, which means that all four virtual cores were used.

## 4
*Could one implement MyAtomicInteger without synchronization, just using a
volatile field? Why or why not?*

No, the `volatile` field is not enough in this case. In fact, we are facing an
instance of the well-known Readers-Writers concurrency problem: readers do not
need to act in mutual exclusion (indeed `volatile` is needed due to Java
implementation), while writers do, which in Java terms means necessity for the
`synchronized` keyword on writing code sections.

## 5
*Solve the same problem but use the AtomicInteger class from the
java.util.concurrent.atomic package instead of MyAtomicInteger. Is there any
noticeable difference in speed or result? Should the AtomicInteger field be
declared final?*

Comparing to the homebrew `MyAtomicInteger` class, when using `ÀtomicInteger` from
Java library, both the wall and the CPU time decreased of roughly 2%, regardless
of if being `final` or not. As expectable, the built-in class performs better than
ours.

```bash
real    0m3.556s
user    0m13.138s
sys     0m0.028s
```
# Exercise 2.4
Source file for all tasks:
**Source file: CachedFactorsCounter.java**

## 1
*Write a method exerciseFactorizer that takes as argument a thread-safe caching
factorizer and calls it from 16 threads as specified above.*

**Source file: CachedFactorsCounter.java**

## 2
*The number of calls to the factorizer should be 115 000. Is it? Measure and note
the execution time for this activity, noting wall-time and CPU-time.*

**Source file: CachedFactorsCounter.java**

Yes, the number of calls to `compute()` is exactly 115000. This happens because
the whole `Memoizer1.compute()` is `synchronized`, meaning that the put-if-absent
operation on the cache is atomic: such premise makes it possible to have no calls
to the `Factorizer.compute()` method with overlapping arguments, that in turn
implies no waste of computations. On the other hand, the output of Unix `time`
utility denotes that almost no concurrency is going on, given the negligible
difference between wall time and CPU time. Finally, this Memoizer implementation
is the slowest, because the heavy computations are not parallelized, defeating the
whole goal of the class itself.

```bash
real    0m57.695s
user    0m59.984s
sys     0m0.805s
```
## 3
*Repeat this experiment with `Memoizer2`. How many times is the factorizer called?
How long does the whole process take? Explain both results.*

`Factorizer.compute()` is called 155757 times, which is more than the strict
necessary: this means that the factors for some numbers are computed more than
once. In fact, the put-if-absent cache operation is no longer atomic, leading to
high chances of overlapping factorizations. At a first glance, the level of
parallelism might look fairly high, since the CPU time is roughly 90% of four
times the wall-time, wich is the optimal situation on a four-threaded machine. A
closer inspection, though, reveals that the high figure of CPU time is due to the
overlapping factorizations, owing to the long time occurring between the check for
cached values and the insertion of new ones.

```bash
real    0m15.093s
user    0m53.485s
sys     0m0.048s
```
## 4
*Repeat this experiment with `Memoizer3`. How many times is the factorizer called?
How long does the whole process take? Explain both results.*

The put-if-absent cache pattern is still not atomic, meaning that the number of
calls to `Factorizer.compute()` is again higher than the bare minimum, sitting at
116277: it is even greater than the previous case, owing to more threads being
blocked after the call, waiting for the `FutureTask` to complete. This is also the
reason why the CPU time differs so much, being indeed significantly lower:
factorization overlap happens much less frequently, since much less time passes
between the cache presence check and the insertion of computed values. However,
the increased number of waiting threads makes the parallelism level difficult to
asses accurately, although is rather high.

```bash
real    0m13.145s
user    0m31.874s
sys     0m0.677s
```

## 5
*Repeat this experiment with `Memoizer4`. How many times is the factorizer called?
How long does the whole process take? Explain both results.*

This time the method `ConcurrentHashMap.putIfAbsent()` is used to implement the
put-if-absent pattern on the cache, thus atomicity is achieved: as a confirmation,
the number of calls to `Factorizer.compute()` drops down to the minimum of 115000.
The wall-CPU time ratio is almost the same as the previous case, meaning that
actual overlapping factorizations were rare in that implementation; the same goes
for the degree of concurrency, which is again significant but not precisely
estimated.

```bash
real    0m13.153s
user    0m31.525s
sys     0m0.797s
```

## 6
*Repeat this experiment with `Memoizer5`. How many times is the factorizer called?
How long does the whole process take? Explain both results.*

Being this an equivalent implementation of the previous case, which only differs
in leveraging on a convenience method, the considerations about the number of
calls to `Factorizer.compute()` and the level of concurrency are the same: bare
minimum of 115000 for the former and remarkable but not accurate for the latter.
The only significant difference are the times being a little smaller, probably due
to the higher optimizaton of built-in implementations.

```bash
real    0m12.961s
user    0m30.245s
sys     0m0.812s
```

## 7
*Write a caching class Memoizer0 that uses ConcurrentHashMap and its
computeIfAbsent method to simply compute the given work c.compute(arg), and using
no FutureTasks or other fancy features. [...] Repeat the above experiment with
your Memoizer0. How many times is the factorizer called? How long
does it take? Explain both results.*

The number of calls to `Factorizer.compute()` is still 115000, the bare minimum,
being this a stripped-down variant of the previous two; the same applies for
parallelization degree. The wall-CPu time ratio is instead higher than the
preceding cases, probably because the lack of fancy features leads to a minor
overhead.

```bash
real    0m13.269s
user    0m37.884s
sys     0m0.338s
```
# Exercise 3

## 1
*Make a thread-safe implementation, class Histogram2, of interface Histogram by adding suitable modifiers
(final and synchronized) to a copy of the Histogram1 class. Which fields and methods need which
modifiers? Why? Does the getSpan method need to be synchronized?

Elements of the counts array will be both read and overwritten so increment and and getCount must be synchronized.
The size of the array does not change which means that it would be good to mark the array as "final" as only resizing the array would change the reference.
This will never happen in this program.
This "final" thus garantees that its safe to have getSpan be nonsyncronized since its return value never changes after initialization of the class itself.
Increment and getCount however must be syncronized as they access and overwrite values and thus there must be visibility for both increment and getCount and atomicity in the case of increment between threads. 

See SimpleHistogram.java

## 2
*Now consider again counting the number of prime factors in a number p, as in Exercise 2.3 and file TestCountFactors.java. Use the Histogram2 class to write a parallel program that counts how many numbers
in the range 0. . . 4 999 999 have 0 prime factors.

See TestCountFactors.java

## 3
*Can you now remove synchronized from all methods? Why? Run your prime factor counter and check
that the results are correct.

AtomicInteger garantees visibility and thread-safe atomic execution of get and increments. There are the also sort of operations we need.
Therefore the answer is YES and you can remove synchronization from all methods.

## 4
*Define a thread-safe class Histogram4 that uses a java.util.concurrent.atomic.AtomicIntegerArray object to
hold the counts. Run your prime factor counter and check that the results are correct

See TestCountFactors.java

Output was:
0 : 2
1 : 348513
2 : 979274
3 : 1232881
4 : 1015979
5 : 660254
6 : 374791
7 : 197039
8 : 98949
9 : 48400
10 : 23251
11 : 11019
12 : 5199
13 : 2403
14 : 1124
15 : 510
16 : 233
17 : 102
18 : 45
19 : 21
20 : 7
21 : 3
22 : 1

## 5
*Now extend the Histogram interface with a method getBins that returns an array of the bin counts:
public int[] getBins();

See TestCountFactors.java

## 6
*Create a Histogram5 class that uses an array of LongAdder objects for the bins, and use it to solve the same
problem as before.

See TestCountFactors.java

# Exercise 5

## 1
*Describe what you observe when you run the program.
The output:
main
main finished 40000000
fresh 0 stops: 22021939
fresh 1 stops: 22022685

Neither thread 1 or 2 reports that count is anywhere near 40000000 so the observation is that we are way below the expected result.

## 2
*How can you explain what you observe?
count is of the type Long which is an immutable object. This means that to allow incrementation, that object must be replaced by a new Long.
This in turn means that the count variable constantly changes in terms of reference which means that each incrementation will lock on another reference.
Example case:
Thread A acquire intrinsic lock on count
Thread B waits for lock
Thread A increment and releases lock
Thread B enters block
Thread A acquire lock on count with new reference due to last incrementation
Thread B and A increment at the same time and, reads the same number and writes the same number.
(Now count is wrong by the offset of 1, no atomicity preserved)

## 3
* Create a version of the program (changing as little as possible) that works as intended
To lock on count we need to lock on a shared immutable object reference that does not change. For that I changed the datatype of Long to AtomicLong which is immutable and thus
I have marked the count field final.
The output is nowmain
main finished 40000000
fresh 0 stops: 39197835
fresh 1 stops: 40000000

40000000 is the expected result.
See TestStaticCounter.java