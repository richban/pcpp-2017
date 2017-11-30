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

I don't know.

## Task 5
Describe and conduct an experiment to cast some light on the role of one of E7 and D5.
...


