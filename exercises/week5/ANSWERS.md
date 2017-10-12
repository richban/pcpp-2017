# Exercise 5.1

* OS:   Mac OS X; 10.12.6; x86_64
* JVM:  Oracle Corporation; 1.8.0_144
* CPU:  null; 8 "cores"
* Date: 2017-10-12T18:40:34+0200

### Task

* py/Executors.newCachedThreadPool.html
* py/Executors.newWorkStealingPool.html

# Exercise 5.3

## Task 2
*Now write a sequential method getPages(urls, maxLines) that given an array urls of
webpage URLs fetches up to maxLines lines of each of those webpages, and returns the
result as a map from URL to the text retrieved.*

*** Source code: TestDownload.java ***

## Task 3
*Use the Timer class — not the Mark6 or Mark7 methods — for simple wall-clock
measurement (described in the Microbenchmarks lecture note from week 4) to measure
and print the time it takes to fetch these URLs sequentially. Do not include the
time it takes to print the length of the webpages. Perform this measurement five
times and report the numbers.*

Results:
```
Pages took 4660739050 ns to load
Pages took 3971414786 ns to load
Pages took 3871589843 ns to load
Pages took 4048708406 ns to load
Pages took 3819702972 ns to load
```

## Task 4
*Now create a new parallel version getPagesParallel of the getPages method that
creates a separate task (not thread) for each page to fetch. It should submit the
tasks to an executor and then wait for all of them to complete, then return the
result as a Map<String,String> as before.

Call it as in the previous question, measure the wall-clock time it takes to
complete, and print that and the list of page lengths. Repeat the measurement five
times and compare the results with the sequential version. Discuss results, in
particular, why is fetching 23 webpages in parallel not 23 times faster than
fetching them one by one?*

*** Source code: TestDownload.java ***

As expected, the execution time is definitely lower, roughly reduced by a half:
this is due to the parallelization of the workload, implemented by tasks and
executors. However, the parallelism does not take place as concurrency, but as
interleaving: indeed, the IO-bound nature of the computation makes it possible to
allocate the CPU to a thread performing calculations, while other ones are blocked
waiting for a network operation to complete (as a side note, this is the main idea
behind the Node.js runtime). This also explains why the running time is not
proportional to the number of tasks: in fact, the actualy concurrency happens on external computing units, whose number is not influenced by that of threads running on the executing machine. In other words, the most significant running time component of this kind programs is IO waiting time, which is, again, not influenced by the number of threads on the computing machine. Following, the timing results:
```
Pages took 2060970497 ns to load
Pages took 1905681240 ns to load
Pages took 1987773504 ns to load
Pages took 1813257937 ns to load
Pages took 1879440503 ns to load
```
