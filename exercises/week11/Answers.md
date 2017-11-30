Group: 23

*Name 1:* Patrick Evers Bjoerkman (pebj@itu.dk)
*Name 2:* Davide Laezza (dlae@itu.dk)
*Name 3:* Richard Banyi (riba@itu.dk)


# Exercise 11.1

## Task 1 - 2
***Source code: TestMSQueue.java***

Does the MSQueue implementation pass the concurrent test?
Yes

## Task 3
Inject some faults in the MSQueue implementation and see whether the test detects them. Describe the
faults and whether the test detects them, and if it does detect them, how it fails.

Removing the while loop in the enqueue will cause the risk of failure whenever compareAndSet fails. This won't happen in a sequential test
of this class, but it will likely happen in a concurrent test setting. It will be detected by the concurrent test never completing, reason being that
dequeue assumes that producers still haven't completed adding the expected number of elements. Truth is that the producers have finished, but the queue
have failed to add several items.

# Exercise 11.2

## Task 1 : Do you agree with this argument?
The only consequence of the tail chancing is the advancement of the tail failing after insertion of the new element. This is not an issue as all other threads
will be forced to advance the tail before they can complete their desired action. Furthermore, with E9 succeeding, it will mean that no other thread succeeding in
changing the variable before the thread succeeding E9. With an additional element added at the tail, the only sensible action would be to advance the tail.
Therefore I would say the answer is that I a gree with E7 not being useful.

## Task 2: Does it pass the test?
Yes.

## Task 3
The tests still passes.

## Task 4
If the checks at lines E7 and D5 are indeed unnecessary for correctness, what other reasons could there be
to include them in the code? How would you test your hypotheses about such reasons?
Performance? Davide?

## Task 5
Describe and conduct an experiment to cast some light on the role of one of E7 and D5.
...


# Exercise 11.3
All the benchmarks are made with 2, 4 and 8 threads, in this order, executing
concurrently on a dual-core hypethreaded CPU.

## Task 1
***Measure performance of the Michael-Scott queue implementation class MSQueue
that uses an AtomicReference<Node<T>> in each Node<T> object.***
```bash
Parallel test: class MSQueue... passed
296

Parallel test: class MSQueue... passed
491

Parallel test: class MSQueue... passed
1039
```

## Task 2
***Measure performance of the Michael-Scott queue implementation class MSQueue
that uses an AtomicReference<Node<T>> in each Node<T> object.***

The performances are surprisingly slightly worse than the previous case.
We don't really understand why, since this solution is better from any point of
view: the `AtomicReferenceFieldUpdater` provides better thread locality, and the
`next` field in the `Node` class is still flushed to main memory.

```bash
Parallel test: class MSQueueRefl... passed
300

Parallel test: class MSQueueRefl... passed
493

Parallel test: class MSQueueRefl... passed
1045
```

## Task 3
***Measure the performance of the lock-based queue implementation and compare
with the two Michael-Scott queue implementations.***

This implementation is slower than the Michael-Scott one, as we were expecting.
Indeed, the expensive `isPrime` operation is executed while holding the lock,
which defeats any actual concurrency purpose.

```bash
Parallel test: class LockingQueue... passed
293

Parallel test: class LockingQueue... passed
513

Parallel test: class LockingQueue... passed
1122
```

## Task 4
***Measure also the performance of a version of the Michael-Scott queue where
the E7 and D5 checks have been removed***

These tests proved correct, even though the checks have been removed. At a first
glance, the lack of any significant improvement in performance is puzzling, but
reasoning about it, if the tests are still passed even without checks, then it
means that such checks were not relevant for the program, at least in this case.

```bash
Parallel test: class MSQueue... passed
314

Parallel test: class MSQueue... passed
533

Parallel test: class MSQueue... passed
1048
```
