Group: 23

*Name 1:* Patrick Evers Bjoerkman (pebj@itu.dk)
*Name 2:* Davide Laezza (dlae@itu.dk)
*Name 3:* Richard Banyi (riba@itu.dk)

# Exercise 8.1
***Source code: TestStripedWriteMap.java***

## Task 1
The functional testing suite doesn't check the `remove()` method in the case when
the element is not present, that is the path corresponding to the `then` branch of
the conditional operator in the method. It also does not test the reallocation
functionalities, but this reflects the precise design choice of not making them
available publicly (the method `reallocateBuckets(final ItemNode<K,V>[])` is
`public`, but the list of buckets `buckets` is `private`).

## Task 2 and Task 3
The test does finds no flaws, neither in `StripedWriteMap` nor in
`WrapConcurrentHashMap`. However, the test is only checking the correctness of
`put()`, `putIfAbsent()`. While `remove()`, while `get()`, `size()` and `forEach()`
are not even called. `containsKey()` is, but its result is not part of the test
(in our code is actually not even taken in account by any means).

___NOTE: Ask the TA why the whole machinery hangs when `return put(k, v)` is
inside the `synchronized` block: aren't lock reentrant?___

## Task 4
***Source code: TestStripedWriteMap.java***


## Task 5
Aside from incrementing/decrementing ints of the counts array as described by the assignment. We chose to not compute by sum to verify the number of elements added
by a thread. Instead we did something that results in the same, but worked better with out previous code, basically instead of computing the sum of elements added
by a particular thread, I looped through every element in the map. For each element I would find which thread added it and decremented its counts int. At the end
we simply need to verify that each counts int has resulted in zero. This will mean that every element from the map is related to a specific element in the counts array.

## Task 6
As the threads need to affect the counts integer of another thread on remove or put, we have that the counts array datatype of int (from Task 4) is not atomic.
A better alternative would be to use AtomicIntegerArray which provides threadsafe atomic operation.

# Exercise 8.2

## Task 1
After removing the `synchronized` in 1 block the test passed *almost* on every run, however when removing from 2 blocks the test failed far more.

```bash
Exception in thread "main" java.lang.AssertionError
	at TestStripedWriteMap.parallelTest(TestStripedWriteMap.java:262)
	at TestStripedWriteMap.main(TestStripedWriteMap.java:32)
```  
## Task 2

Likewise on previous task 1, on some occurrences the the tests passed.

## Task 3

The functional test discovered correctly that the `sizes` are not correctly updated.

## Task 4

The absence of atomic reads did not affect the results of the tests. All tests passed.

## Task 5
Essentially, we can try to undermine all of the concurrency features used in the
implementation. Among those not already tested, we can suggest making `buckets`
not `volatile`: this would cause many calls to `ItemNode` methods to operate on
out-of-sync data, leading to inconsistencies in the data structure. In particular,
this would change, compared to a proper update of the buckets, whether or not
`put()` and `putIfAbsent()` actually insert a pair, and if `remove()` pops it,
making every thread compute a different key sum than expected.
