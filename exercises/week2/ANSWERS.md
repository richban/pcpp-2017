# Exercise 1
## 1
*Write a sequential program to compute the total number of prime factors of the integers in range 0 to 4,999,999. The result should be 18,703,729. How much time does this take?*

**Source file: SerialFactorsCounter.java**
The Unix `time` output for the program is:

```bash
real    0m7.130s
user    0m7.131s
sys     0m0.008s
```
In addition to showing that my laptop is clearly not a computing powerhouse, the output confirms that no parallelism is happening.

## 2
*Write such a MyAtomicInteger class.*

**Source file: MyAtomicInteger.java**

## 3
*Write a parallel program that uses 10 threads to count the total number of prime factors of the integers in range 0 to 4,999,999. [...] Do you still get the correct answer? How much time does this take?*

**Source file: ParallelFactorsCounter.java**

The answer is the same as in the seial case, that is 18703729. The `time` information, however, differs significantly:

```bash
real    0m3.568s
user    0m13.377s
sys     0m0.024s
```
On my dual-core, hyper threaded CPU the wall time is approximately one fourth of the CPU time, which means that all four virtual cores were used.

## 4
*Could one implement MyAtomicInteger without synchronization, just using a volatile field? Why or why not?*

No, the `volatile` field is not enough in this case. In fact, we are facing an instance of the well-known Readers-Writers concurrency problem: readers do not need to act in mutual exclusion (indeed `volatile` is needed due to Java implementation), while writers do. which in Java terms means necessity for the `synchronized` keyword on writing parts.

## 5
*Solve the same problem but use the AtomicInteger class from the java.util.concurrent.atomic package instead of MyAtomicInteger. Is there any noticeable difference in speed or result? Should the AtomicInteger field be declared final?*

Comparing to the homebrew `MyAtomicInteger` class, when using `ÀtomicInteger` from Java library, both the wall and the CPU time increased of roughly 11%, regardless of if being `final` or not. We do not know why this happens, and can only speculate that our lightweight, case-suited class performs better in these conditions. Following, the unic `time` command output:

```bash
real    0m3.919s
user    0m14.927s
sys     0m0.020s
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