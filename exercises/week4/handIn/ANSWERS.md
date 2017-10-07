Group: 23
*Name 1: Patrick Evers Bjoerkman (pebj@itu.dk)
*Name 2: Davide Laezza (dlae@itu.dk)
*Name 3: Richard Banyi (riba@itu.dk)

# Exercise 4.1
*** Source code: Benchmark.java **
*** Benchmark results: Results 4.1.txt **

## Mark0
From this measurement we can conclude nothing at all, because we are certainly
measuring the time it takes the JVM to start up, which is much larger than
the time to actually run the code.

## Mark1
Just as in `Microbenchmarks` our results are also unrealistic. We encountered
the so-called "dead code elimination". Count is really large and the compiler
notices that we are running the for-loop without using the `multiply` variable,
therefore it optimizes and removes the for-loop.

## Mark2
Running `Mark2()` we got more consistent and plausible results, because we actually
use the result of the for-loop, which means it can not be optimized by the compiler.

## Mark3
All the iterations take nearly the same amount of time as in `Mark2()`. In this case
the running time varied slightly.

## Mark4
In this case the standard deviation out of ten values is also provided. Its low
value shows that all the running times are similar.

## Mark5
In the first run we can tell that the compiler did not no optimazation.
Following each other running tests we can see slight improvements
of the running time, each cycle we can assume that compiler tried for optimazation.
Expect the sudden increase to 2962.7 at iteration count 64, which can be caused
by other external running processes. From 131072 onwards there is no performance
gain, as the code is both cached and compiled for the majority of the iterations.

## Mark6
In the fists test the overhead of the extra object in the method arguments is
still significant, because the cache is not boosting the performances. As the
number of iterations grows the impact of such overhead decreases, up to the point
that we reach the same performances as `mark5()`, although with a greater number
of iteration. 

## Mark7
After a certain iterations we can assume that the optimizations performed by
JIT doesn't increase and does not produce new machine code which is eventually
cached and therefore the runtime eventually stabilize. However these results are
influenced by other external processes.

# Exercise 4.2

## Mark6
Creating an objects in Java took us around 70 ns. While creating a Thread took us
around 898.8 ns. Altought Java defines Threads as objects they're in reallity more
complex than a ordinary objects which are only available for the JVM and require a
fair bit more work involved.

## Mark7
Unlike Java Objects, thread (objects) creation is expensive because under the hood
there is a bit of work, especially registration with the host OS. Altough starting
a thread takes up much more time because the OS has to manage and schedule the
threads. The actual work took about  52887.8 ns which heavly depends on the
`.join()` which is waiting idle for the other thread to finish. Here, the output of the program.

```
# OS:   Mac OS X; 10.12.6; x86_64
# JVM:  Oracle Corporation; 1.8.0_144
# CPU:  null; 8 "cores"
# Date: 2017-09-28T20:26:07+0200
hashCode()                            2.7 ns       0.04  134217728
Point creation                       74.6 ns       1.13    4194304
Thread's work                      5765.2 ns      83.96      65536
Thread create                       883.5 ns       5.72     524288
Thread create start               38290.5 ns     132.28       8192
Thread create start join          52887.8 ns     584.81       8192
ai value = 1638340000
Uncontended lock                      5.5 ns       0.06   67108864
```

# Exercise 4.3

## Task 1: Measure the performance of the prime counting example on your own
hardware, as a function of the number of threads used to determine whether a given
number is a prime. Record system information as well as the measurement results
for 1. . . 32 threads in a text file. If the measurements take excessively long
time on your computer, you may measure just for 1. . . 16 threads instead

*** Results: Exercises4.3.1.txt ***

## Task 2: Use Excel or gnuplot or Google Docs online or some other charting
package to make graphs of the execution time as function of the number of threads.

*** Charts in: BenchmarkExcel.png ***

## Task 3: Reflect and comment on the results; are they plausible? Is there any
reasonable relation between the number of threads that gave best performance, and
the number of cores in the computer you ran the benchmarks on? Any surprises?

The chart shows a relation between my hardware being a four core machine and that
after four threads, we observe that performance stops improving. Going over core
count seem to cause greater overhead that ends up eating performance away. It
basically shows that parallizing without considering the hardware hurts the
performance.

## Task 4: Now instead of the LongCounter class, use the
java.util.concurrent.atomic.AtomicLong class for the counts. Perform the
measurements again as indicated above. Discuss the results: is the performance of
AtomicLong better or worse than that of LongCounter? Should one in general use
adequate built-in classes and methods when they exist?

*** Chart in: BenchmarkExcel2.png ***

The performance of AtomicLong is worse than LongCounter. That said it is
interesting that AtomicLong becomes most efficient when dealing with around 11
threads.

## Task 5: Now change the worker thread code in the lambda expression to work like
a very performance-conscious developer might have written it. Instead of calling
lc.increment() on a shared thread-safe variable lc from all the threads, create a
local variable long count = 0 inside the lambda, and increment that variable in
the for-loop. This local variable is thread-confined and needs no synchronization.
After the for-loop, add the local variable’s value to a shared AtomicLong, and at
the end of the countParallelN method return the value of the AtomicLong. This
reduces the number of synchronizations from several hundred thousands to at most
threadCount, which is at most 32. In theory this might make the code faster.
Measure whether this is the case on your hardware. Is it? (It is not faster on my
Intel-based MacOS laptop).

*** Source code: TestCountPrimesThreads.java ***
*** Program output: Output4.3.5.txt ***
*** Chart in: BenchmarkExcel3.png ***

It is not faster, but it definitely have a positive effect on higher threadcount
compared to the one without it.

# Exercise 4.4

## Tasks 1 - 6
***Source code: TestMemoizers.java, TestCache.java***

Results:
```bash
# OS:   Linux; 4.12.0-1-amd64; amd64
# JVM:  Oracle Corporation; 1.8.0_144
# CPU:  null; 4 "cores"
# Date: 2017-09-28T18:51:24+0200
Memoizer0                      22248639.4 ns  495029.17         16
Memoizer1                      56028736.9 ns 1270861.63          8
Memoizer2                      12206940.3 ns  805258.19         32
Memoizer3                      13442019.3 ns  919974.19         32
Memoizer4                      13434034.4 ns 1069905.90         32
Memoizer5                      25209457.2 ns  551591.10         16
# OS:   Linux; 4.12.0-1-amd64; amd64
# JVM:  Oracle Corporation; 1.8.0_144
# CPU:  null; 4 "cores"
# Date: 2017-09-28T18:51:24+0200
Memoizer0                      23245455.0 ns  528122.73         16
Memoizer1                      56478651.9 ns 1810874.79          8
Memoizer2                      12305846.8 ns  738427.42         32
Memoizer3                      13575847.9 ns  724534.16         32
Memoizer4                      13697237.5 ns  856626.51         32
Memoizer5                      24489565.8 ns  346467.73         16
```

# Task 7
`Memoizer1` is the slowest of all implementations, due to its substantial lack of
parallelism. Then `Memoizer0` and `Memoizer5` complete in roughly half of the time
than `Memoizer1`; their shared trait, which also makes them different from the
others, is the use of `computeIfAbsent()`: being Java an eager language, this
method always creates the `Function` object, even when it is not used because the
value is already cached, which leads to overhead in terms of memory access. The
fastest ones, taking roughly the same time, are `Memoizer2`, `Memoizer3` and
`Memoizer4`: this is surprising, for the first is expected to be slower due to
repeated computations. What actually happens is that such reiterated calculations
are fewer than expected, due to the sequential factorization of all the numbers in
an executor interval, which makes `Memoizer2` faster than the other two due to the
lack of overhead in creating `Future` and `Callable` machinery.

# Task 8
The most important factor in an experiment on parallel memoization scalability is
the amount of overlapping work: as it decreases the possibilities of actual
concurrency rise, since the treads are less likely to be idle waiting for each
other. On the other hand, blocked threads free CPU cores, which means that it is
efficient to have more threads than available processing units. The last factor to
takee in account is randomization: sequential processing of elements of a list
allows for additional assumptions, which means that both scenarios should be
considered in generic experiments.
That said, a possible experiment would involve processing the elements of a list in
parallel. First, the amount of common items is varied, while keeping the number of
threads constant; second, the vice-versa; and finally, the variations are i
interleaved in order to reach the maximum CPU usage throughout a single run of the
experiment. This should then be tested on both sequential and random access.
