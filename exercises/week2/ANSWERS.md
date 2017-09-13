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
real    0m3.617s
user    0m13.535s
sys     0m0.020s
```
On my dual-core, hyper threaded CPU the wall time is approximately one fourth of the CPU time, which means that all four virtual cores were used.

## 4
*Could one implement MyAtomicInteger without synchronization, just using a volatile field? Why or why not?*

No, the `volatile` field is not enough in this case. In fact, we are facing an instance of the well-known Readers-Writers concurrency problem: readers do not need to act in mutual exclusion (indeed `volatile` is needed due to Java implementation), while writers do, which in Java terms means necessity for the `synchronized` keyword on writing code sections.

## 5
*Solve the same problem but use the AtomicInteger class from the java.util.concurrent.atomic package instead of MyAtomicInteger. Is there any noticeable difference in speed or result? Should the AtomicInteger field be declared final?*

Comparing to the homebrew `MyAtomicInteger` class, when using `ÀtomicInteger` from Java library, both the wall and the CPU time decreased of roughly 2%, regardless of if being `final` or not. As expectable, the built-in class performs better than ours.

```bash
real    0m3.556s
user    0m13.138s
sys     0m0.028s
```
