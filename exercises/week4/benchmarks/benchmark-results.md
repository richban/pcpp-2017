# Exercise 1

## Exercise 4.1

###### Mark0
From this measurement we can conclude nothing at all, because we are certainly
measuring the time it takes the JVM to start up, which is much larger than
the time to actually run the code.

###### Mark1
Just as in `Microbenchmarks` our results are also unrealistic. We encountered
the so-called "dead code elimination". Count is really large and the compiler
notices that we are running the for-loop without using the `multiply` variable,
therefore it optimizes and removes the for-loop.

###### Mark2
Running `Mark2()` we got more consistent and plausible results, because we actually
use the result of the for-loop, which means it can not be optimized by the compiler.

###### Mark3
All the iterations take nearly the same amount of time as in `Mark2()`. In this case
the running time varied slightly.

###### Mark4

###### Mark5
In the first run we can tell that the compiler did not no optimazation.
Following each other running tests we can see slight improvements
of the running time, each cycle we can assume that compiler tried for optimazation.
Expect the sudden increase to 2962.7 at iteration count 64, which can be caused
by other external running processes. From 131072 onwards there is no performance gain,
as the code is both cached and compiled for the majority of the iterations.

###### Mark6
In the fists test the overhead of the extra object in the method arguments is
still significant, because the cache is not boosting the performances. As the
number of iterations grows the impact of such overhead decreases, up to the point
that we reach the same performances as `mark5()`, although with a greater number
of iteration.

```
# OS:   Mac OS X; 10.12.6; x86_64
# JVM:  Oracle Corporation; 1.8.0_144
# CPU:  null; 8 "cores"
# Date: 2017-09-28T17:40:01+0200

4951.0 ns

   0.0 ns

  30.9 ns

  30.8 ns
  30.9 ns
  30.5 ns
  30.7 ns
  30.9 ns
  31.0 ns
  30.6 ns
  30.8 ns
  30.5 ns
  30.7 ns

  30.7 ns +/-  0.288

 186.0 ns +/-    87.07          2
  99.5 ns +/-    36.42          4
  83.2 ns +/-    19.15          8
  51.3 ns +/-    18.91         16
  73.5 ns +/-   104.61         32
2962.7 ns +/-  9215.94         64
  40.5 ns +/-     7.49        128
  41.1 ns +/-    12.22        256
  39.8 ns +/-     7.63        512
  36.9 ns +/-     4.18       1024
  35.3 ns +/-     3.50       2048
  35.6 ns +/-     2.12       4096
  34.4 ns +/-     3.63       8192
  32.3 ns +/-     0.97      16384
  35.4 ns +/-     3.32      32768
  36.9 ns +/-     8.25      65536
  31.8 ns +/-     1.66     131072
  30.4 ns +/-     0.62     262144
  31.5 ns +/-     1.66     524288
  30.8 ns +/-     1.07    1048576
  31.2 ns +/-     1.43    2097152
  31.2 ns +/-     0.71    4194304
  30.9 ns +/-     0.42    8388608

multiply                            819.7 ns    1432.87          2
multiply                            159.7 ns      61.11          4
multiply                            159.5 ns      83.11          8
multiply                            129.5 ns     141.28         16
multiply                             69.6 ns      65.86         32
multiply                             54.7 ns      24.50         64
multiply                             50.0 ns      11.22        128
multiply                             54.1 ns      13.11        256
multiply                             49.7 ns      15.12        512
multiply                             42.4 ns       0.43       1024
multiply                             42.2 ns       0.24       2048
multiply                             42.4 ns       1.46       4096
multiply                             36.4 ns       2.49       8192
multiply                             38.8 ns      11.74      16384
multiply                             32.6 ns       2.27      32768
multiply                             33.5 ns       5.33      65536
multiply                             35.1 ns       6.90     131072
multiply                             31.9 ns       2.67     262144
multiply                             32.5 ns       2.10     524288
multiply                             31.1 ns       1.82    1048576
multiply                             30.9 ns       1.57    2097152
multiply                             31.2 ns       0.88    4194304
multiply                             30.7 ns       0.74    8388608
```

###### Mark7
After a certain iterations we can assume that the optimizations performed by
JIT doesn't increase and does not produce new machine code which is eventually
cached and therefore the runtime eventually stabilize. However these results are
influenced by other external processes.

```
# OS:   Mac OS X; 10.12.6; x86_64
# JVM:  Oracle Corporation; 1.8.0_144
# CPU:  null; 8 "cores"
# Date: 2017-09-28T19:15:44+0200
pow                                  76.7 ns       0.87    4194304
exp                                  55.1 ns       0.56    8388608
log                                  25.3 ns       0.37   16777216
sin                                 121.7 ns       1.85    2097152
cos                                 121.4 ns       1.30    2097152
tan                                 158.9 ns       5.70    2097152
asin                                228.4 ns       2.11    2097152
acos                                212.6 ns       2.20    2097152
atan                                 52.0 ns       0.50    8388608
```

## 4.2

###### Mark6
Creating an objects in Java took us around 70 ns. While creating a Thread took us around 898.8 ns. Altought
Java defines Threads as objects they're in reallity more complex than a ordinary objects which are only available
for the JVM and require a fair bit more
work involved.

###### Mark7
Unlike Java Objects, thread (objects) creation is expensive because under the hood there is a bit of work
especially registration with the host OS. Altough starting a thread takes up much more time because the OS has
to manage and schedule the threads. The actual work took about  52887.8 ns which heavly depends on the
`.join()` which is waiting idle for the other thread to finish.

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
