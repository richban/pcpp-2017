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
# Exercise 2

## 1
*Make a thread-safe implementation, class Histogram2, of interface Histogram by adding suitable modifiers
(final and synchronized) to a copy of the Histogram1 class. Which fields and methods need which
modifiers? Why? Does the getSpan method need to be synchronized?

Elements of the counts array can be read and incremented and as such a solution would not be to have an immutable class. However the size of the array does not change which means that it would be good to mark the array as "final", this garantees that no code will be able to change the referenced array and the referenced array has a fixed size.
This "final" thus garantees that its safe to have getSpan be nonsyncronized since its return value never changes af initialization of the class itself.
Increment and getCount however must be syncronized as they access and overwrite values and thus there must be visibility between threads. 

See SimpleHistogram.java

## 2

See TestCountFactors.java

## 3
*Can you now remove synchronized from all methods? Why? Run your prime factor counter and check
that the results are correct.

AtomicInteger garantees visibility and thread-safe atomic execution of get and increments. Therefore since this is no other atomicity needed then the answer is YES you can remove synchronization from all methods.

## 4
See TestCountFactors.java

## 5
See TestCountFactors.java

## 6
See TestCountFactors.java