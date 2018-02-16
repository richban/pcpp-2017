Testing LinkedListStack:

*Implementation:* ConcurrentStack.java

For the correctness of the functional requirements I have created a very
basic single-threaded functional testing while implementing the stack based
linked list, the method name is called *seqTest*.

Also I have creating a *parallelTest* with N-threads with aggregate results.
For this test I have used the **CyclicBarrier(N)** and used the Consumer & Producer
pattern. First I create *npair* of Producers and Consumers. The Producers
pushes *nTrials* random numbers and the consumer pops *nTrials* from the stack.
Afterwards I check the consumed numbers is equals to the sum of the produced
numbers. Both of them are summing a thread-local *sum* variable and than adding
the result to a common  AtomicInteger.

Results:

```
Parallel test: class ConcurrentStackImp... passed
time = 30815666.00 ns; threadCount = 9
Parallel test: class ConcurrentStackImp... passed
time = 98779293.00 ns; threadCount = 13
Parallel test: class ConcurrentStackImp... passed
time = 83835107.00 ns; threadCount = 17
Parallel test: class ConcurrentStackImp... passed
time = 194371707.00 ns; threadCount = 21
```
