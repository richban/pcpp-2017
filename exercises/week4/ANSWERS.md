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
