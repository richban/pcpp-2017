Group: 23

*Name 1:* Patrick Evers Bjoerkman (pebj@itu.dk)
*Name 2:* Davide Laezza (dlae@itu.dk)
*Name 3:* Richard Banyi (riba@itu.dk)

# Exercise 9.1

## Task 1
***Source code: TestStmHistogram.java***

## Task 2
***Source code: TestStmHistogram.java***
The code produces the same output as provided in the assignment. Console dump also confirms that the
sum of prime numbers amounts to 4000000.

## Task 3-4
***Source code: TestStmHistogram.java***

## Task 5
***Source code: TestStmHistogram.java***
Implemented by option b. The source code iterates every bin and creates a transaction for each based on transferbins and getAndClear.

## Task 6
In accordance with the assignment, transferbins method should be occationally executed after every thread has started. For this to make sense it should be done before
the main thread awaits the stopBarrier. However doing it this way will have the risk that some thread haven't completed within 200 calls to transferbins. This will in
turn mean that the thread might increment the histogram which would not be transfered to total before dump(total) is called. For this reason I have added an additional
call to transferbins after every thread completes, hence after the main thread passes awaiting stopBarrier. With this it is ensured that  total should be what histogram’s
counts used to be and for histogram to only contain empty bins. Furthermore this last call to transferbins won't have any compitition with any other transaction and therefore
will not fail unlike some calls to transferbins before awaiting stopBarrier might.

## Task 7
What effect would you expect total.transferBins(total) to have? What effect does it have in
your implementation? Explain.
For every bin, to create an increment transaction, the return value of getAndClear will be required. For this reason, a getAndClear transaction must complete before
an increment transaction can be created. GetAndClear followed by increment will be executed once for every bin. Thus no conflict will occur and we will see the result
as total not having changed. A test confirmed this.