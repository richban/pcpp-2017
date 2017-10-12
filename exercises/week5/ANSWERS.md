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
# OS:   Linux; 4.12.0-2-amd64; amd64
# JVM:  Oracle Corporation; 1.8.0_144
# CPU:  null; 4 "cores"
# Date: 2017-10-11T14:46:26+0200

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
# OS:   Linux; 4.12.0-2-amd64; amd64
# JVM:  Oracle Corporation; 1.8.0_144
# CPU:  null; 4 "cores"
# Date: 2017-10-11T14:46:26+0200

Pages took 2060970497 ns to load
Pages took 1905681240 ns to load
Pages took 1987773504 ns to load
Pages took 1813257937 ns to load
Pages took 1879440503 ns to load
```
# Exercise 5.4
***Source file: TestPipeline.java***

## Task 3
*The results should be the same as when using threads. Are they?*

In our initial test on a four-core machine, the program hangs before processing any
input completely. In fact, since WorkStealingPool creates by default as many
threads as the available cores, only four threads are utilized to execute tasks.
Furthermore, WorkStealingPool is implemented in such a way that the only situation
in which a thread embarks in a new task is when it has completed the one it is
currently working on. In our case, this means that one task will never be
scheduled, since their execution needs to be interleaved: at a certain point, all
four threads will be blocked waiting for the remaining task to carry out its job
with its newly produced input, but such task will never be executed because no
threads are available to carry it out.
Therefore, we changed our implementation to give WorkStealingPool a hint about the
minimum number of threads necessary to execute the job; in addition, we also tested
both our implementations on another machine with eight cores. In both cases, the
results are the same as the threads implementation.

## Task 5
*What so you observe? Can you explain why?*
The same as with task 3, there is no output. The reason is that FixedThreadPool
creates a fixed number of threads. When a task is submitted it will be queued and a
task will be executed in a thread when one is available. There are five tasks that
all blocks, but a fixed number of threads so a task will be queued forever without
being executed.

## Task 6
*You should get the same results as before, though possibly in a different order.
Do you? Why?*

The order is different because the faster PageGetter pushes its WebPage in the
queue first, regardeless of the order they were emitted by the URLProducer.

### Task 7
*Explain why your bounded blocking queue works and why it is thread-safe.*

The internal queue implementation uses two indexes: `head` is used when pushing
elements, and since it marks the last occupied position, is to be circularly
incremented before a new item is inserted; `last` is used when popping elements,
and it is circluarly incremented after the element is inserted, because it points
to the first free position. In order to differentiate an empty queue from a full
one, empty elements are marked with a null value, which has the additional benefit
of clearing unused reference to objects.
Concurrency wise, the two accessor methods are `syncornized`, due to every
operation inside them needing either atomicity or lock acquisition. As the code
spells, in `put()` a thread waits until the queue is no longer full, while in
`take()` a thread is blocked until the queue is not empty anymore. At the end of
both methods `notifyAll()` is called, so as to unblock waiting threads, and
possibly allowing them to complete the method execution. `while` constructs are
used to check waiting conditions in order to face spurious wake-up and due to the
usage of `notifyAll()`. `notify()` could have been used as well, but it would have
required two separate objects used as condition variables.
