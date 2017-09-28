Group: 23
*Name 1: Patrick Evers Bjoerkman (pebj@itu.dk)
*Name 2: Davide Laezza (dlae@itu.dk)
*Name 3: Richard Banyi (riba@itu.dk)

# Week 4

### Exercise 4.3

## 1: Measure the performance of the prime counting example on your own hardware, as a function of the number
of threads used to determine whether a given number is a prime. Record system information as well as the
measurement results for 1. . . 32 threads in a text file. If the measurements take excessively long time on
your computer, you may measure just for 1. . . 16 threads instead

See Exercises4.3.1.txt

## 2: Use Excel or gnuplot or Google Docs online or some other charting package to make graphs of the execution
time as function of the number of threads.

See BenchmarkExcel.png

## 3: Reflect and comment on the results; are they plausible? Is there any reasonable relation between the number
of threads that gave best performance, and the number of cores in the computer you ran the benchmarks on?
Any surprises?

The graph shows a relation between my hardware being a four core machine and that after four threads, we observe that performance stops improving.
Going over core count seem to cause greater overhead that ends up eating performance away. It basically shows that parallizing without considering the
hardware hurts the performance.

## 4: Now instead of the LongCounter class, use the java.util.concurrent.atomic.AtomicLong class for the counts.
Perform the measurements again as indicated above. Discuss the results: is the performance of AtomicLong
better or worse than that of LongCounter? Should one in general use adequate built-in classes and methods
when they exist?

See See BenchmarkExcel2.png
The performance of AtomicLong is worse than LongCounter. That said it is interesting that AtomicLong becomes most efficient
when dealing with around 11 threads.

## 5: Now change the worker thread code in the lambda expression to work like a very performance-conscious
developer might have written it. Instead of calling lc.increment() on a shared thread-safe variable
lc from all the threads, create a local variable long count = 0 inside the lambda, and increment that
variable in the for-loop. This local variable is thread-confined and needs no synchronization. After the
for-loop, add the local variable’s value to a shared AtomicLong, and at the end of the countParallelN
method return the value of the AtomicLong.
This reduces the number of synchronizations from several hundred thousands to at most threadCount,
which is at most 32. In theory this might make the code faster. Measure whether this is the case on your
hardware. Is it? (It is not faster on my Intel-based MacOS laptop).

See TestCountPrimesThreads.java
See BenchmarkExcel3.png, it is not faster, but it definitely have a positive effect on higher threadcount compared to the one without it.
