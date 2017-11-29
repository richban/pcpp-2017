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

