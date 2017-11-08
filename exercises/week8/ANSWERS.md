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

# Exercise 8.2

## Task 5
