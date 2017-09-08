# Exercise 1.1

## 1.1
*What kind of final values do you get when the increment method is not synchronized?*

One thread could interrupt the second one and increment `counter` after the first has read the variable but before it writes the incremented value: this would result in a lost update of `counter`. Following, the results:
Count is 14193731 and should be 20000000
Count is 19796814 and should be 20000000

## 1.2
*Explain how this could be. Would you consider this software
correct, in the sense that you would guarantee that it always gives 200?*

Since the whole counting loop in each thread is faster, the chance of them interleaving executions are much lower. This absolutely doesn't mean that the program always executes correctly.

## 1.3
*Do you think it would make any difference to use one of these forms instead? Why? Change the code and run it. Do you see any difference in the results for any of these alternatives?*

No, there are no differences in the result. This is simply because all the three forms are syntactic sugar for the same sequence of CPU instructions.

## 1.4
*What should the final value be, after both threads have completed? [...] So do the methods have to be synchronized for the example to produce the expected final value? Explain why (or why not).*

The result should be zero. In order to obtain it the two methods need to be `synchronized`, so that those portions of code are mutually-exclusive.

## 1.5
#### None synchronized
Since neither of `increment()` or `decrement()` are `synchronized` the lost update problem is frequent and results are wrong. The execution is the fastest of all four cases, because no locking overhead is present. Some results:
Count is -8784488 and should be 0
Count is 1780341 and should be 0
Count is 9315452 and should be 0
Count is -136416 and should be 0

#### `decrement()` synchronized
Our expectations were not fullfilled: we forecast the results to be comparable to the previous case, while they're much smaller in absolute value. One possible explanation would be that there is less concurrency actually happening: the `increment()` method has some overhead caused by the locking mechanism, thus it executes slower than `decrement()`, making many of the calls subsequent the end of the other thread execution. Results are as follow:
Count is 16700 and should be 0
Count is 21351 and should be 0
Count is 13160 and should be 0
Count is 22860 and should be 0

#### `increment()` synchronized
This is the mirror case of the previous one: one thread terminates before the other,
so many of the method calls are not concurrent. Indeed in this case, where `increment()` executes on its own, return values are all negative, while in the previous one they were all positive. Some samples:
Count is -2994 and should be 0
Count is -19278 and should be 0
Count is -6101 and should be 0
Count is -18471 and should be 0

### Both synchronized
As expectable, when both the methods are synchronized, the results is always correct. Also unsurprisingly, the speed of execution is the lower of all four cases, since no parallelism is actually going on.
Count is 0 and should be 0
Count is 0 and should be 0
Count is 0 and should be 0
Count is 0 and should be 0

# Exercise 1.2
## 1
*Describe a scenario involving the two threads where this happens.*

Thread 1 writes the vertical bar, then the system schedules Thread 2 which prints both the vertical bar and the dash and finally Thread 1 resumes printing a dash again.

## 2
*Making method print synchronized should prevent this from happening. Explain why.*
The synchronized keyword on a method creates an exclusive lock on the instance object of Printer. Since both threads are accessing the same instance of Printer, this locking will ensure mutual exclusion on the print() method, hence making the race condition that results in improper output impossible.

## 3
*Rewrite print to use a synchronized statement in its body instead of the method being synchronized.* 

See Printer.java and PrinterTester.java

## 4
*Make the print method static, and change the synchronized statement inside it to lock on the Print class’s reflective Class object instead.*

See Printer.java and PrinterTester.java

# Exercise 1.3
## 1
*Do you observe the same problem as in the lecture, where the "main" thread’s write to mi.value remains invisible to the t thread, so that it loops forever?*

Yes, the `t` thread hangs. This is because `t` is accessing the instance of `MutableInteger` in the cache memory of the CPU core it is running on, which is not the same instance updated by the main thread.

## 2
*Now declare both the get and set methods synchronized, compile and run. Does thread t terminate as expected now?*

Yes, the program executes as expected. Indeed, the `synchronized` keyward in Java not only ensures mutual exclusion, but also guarantees visibility, that is the values are read and written from and to the main memory.

## 3
*Now remove the synchronized modifier from the get methods. Does thread t terminate as expected now? If it does, is that something one should rely on? Why is synchronized needed on both methods for the reliable communication between the threads?*

No, the `t` thread hangs again. This is due to the lack of visibility of the value stored in the `MutableInteger` instance when it is read.
However, in some cases the program still terminates correctly: in fact, it might happen that, due to the way threads are scheduled, the value is set before the other thread first reads it. Nevertheless, this phenomenon is absolutely casual and not to be relied upon.
This bug arised when the `synchronized` keyword was removed: in fact, in addition to mutual exclusion, it also guarantees visibility.

## 4
*Remove both synchronized declarations and instead declare field value to be volatile. Does thread t terminate as expected now? Why should it be sufficient to use volatile and not synchronized in class MutableInteger?*

Yes, the program executes as expected again. Indeed, the `sychronized` keyword is redundant in this case, because the bug is caused by lack of visibility, and not by absence of atomicity: in fact, all write operations on the shared resource don't require reading any shared resource first. This means that the `volatile` keyword alone is sufficient to solve it.

# Exercise 1.4
## 1
*Run the sequential version on your computer and measure its execution time.*

In windows powershell, using Measure-Command, it took 6699ms.

## 2
*Now run the 10-thread version and measure its execution time; is it faster or slower than the sequential version?*

It is faster, 10 thread version takes 3152ms according to Measure-Command in windows powershell.

## 3
*Try to remove the synchronization from the increment() method and run the 2-thread version. Does it still produce the correct result (664,579)?*

No, because incrementing using `count = count + 1` is not atomic. Furthermore according to Java Concurrency in Practice, 64-bit long and double values to be treated as two 32-bit values. Java language specification for Java 8 at the bottom of this link: (http://docs.oracle.com/javase/specs/jls/se8/html/jls-17.html#jls-17.7 also says the following): a single write to a non-volatile long or double value is treated as two separate writes: one to each 32-bit half. This can result in a situation where a thread sees the first 32 bits of a 64-bit value from one write, and the second 32 bits from another write. In other words writing/reading long are not treatsafe or atomic regardless of using 32bit or 64bit and as such count should be made volatile, be turned into AtomicLong or have some sort of synchronization like before.

## 4
*In this particular use of LongCounter, does it matter in practice whether the get method is synchronized? Does it matter in theory? Why or why not?*

The `get()` method could be not `synchronized` provided that the `count` variable is made `volatile`: indeeed, there are no issues strictly related to concurrency about reading from memory, the problem being related to core-local cache memories instead. Therefore, from a theoretical concurrency point of view no adjustments are necessary, but in practice some are.

# Exercise 1.5
## 1
*Show the results you get. Do they indicate that class Mystery is thread-safe or not?*

The results are incorrect, which means that the Mystery class is not thread-safe. Below, the output of 20 executions of the program.
Sum is 1482544.000000 and should be 2000000.000000
Sum is 1484057.000000 and should be 2000000.000000
Sum is 1478911.000000 and should be 2000000.000000
Sum is 1514773.000000 and should be 2000000.000000
Sum is 1560010.000000 and should be 2000000.000000
Sum is 1490995.000000 and should be 2000000.000000
Sum is 1515199.000000 and should be 2000000.000000
Sum is 1498257.000000 and should be 2000000.000000
Sum is 1479810.000000 and should be 2000000.000000
Sum is 1548801.000000 and should be 2000000.000000
Sum is 1687715.000000 and should be 2000000.000000
Sum is 1712156.000000 and should be 2000000.000000
Sum is 1511491.000000 and should be 2000000.000000
Sum is 1514551.000000 and should be 2000000.000000
Sum is 1570189.000000 and should be 2000000.000000
Sum is 1515587.000000 and should be 2000000.000000
Sum is 1662234.000000 and should be 2000000.000000
Sum is 1472523.000000 and should be 2000000.000000
Sum is 1606210.000000 and should be 2000000.000000
Sum is 1576142.000000 and should be 2000000.000000

## 2
*Explain why class Mystery is not thread-safe.*

The Mystery class is not thread-safe because of the difference in the locking mechanism for instance method and static ones: the former block on the instance they are called on, while the latter on the instance of the metaclass for the class they belong to. Therefore, because the mutual exclusion happpens on two different objects, mutual exclusion is faulty.

## 3
*Explain how you could make the class thread-safe, without changing its sequential behavior.*

Mystery class has been made thread-safe by having the addInstance() method acquire the lock of the same object as the static methods, that is Mystery.class. The oposite is not possible, since static methods cannot access the instance object. Following, the results.
Sum is 2000000.000000 and should be 2000000.000000
Sum is 2000000.000000 and should be 2000000.000000
Sum is 2000000.000000 and should be 2000000.000000
Sum is 2000000.000000 and should be 2000000.000000
Sum is 2000000.000000 and should be 2000000.000000
Sum is 2000000.000000 and should be 2000000.000000
Sum is 2000000.000000 and should be 2000000.000000
Sum is 2000000.000000 and should be 2000000.000000
Sum is 2000000.000000 and should be 2000000.000000
Sum is 2000000.000000 and should be 2000000.000000
Sum is 2000000.000000 and should be 2000000.000000
Sum is 2000000.000000 and should be 2000000.000000
Sum is 2000000.000000 and should be 2000000.000000
Sum is 2000000.000000 and should be 2000000.000000
Sum is 2000000.000000 and should be 2000000.000000
Sum is 2000000.000000 and should be 2000000.000000
Sum is 2000000.000000 and should be 2000000.000000
Sum is 2000000.000000 and should be 2000000.000000
Sum is 2000000.000000 and should be 2000000.000000
Sum is 2000000.000000 and should be 2000000.000000

# Exercise 1.6
## 1
*Explain the simplest natural way to make class DoubleArrayList thread-safe so it can be used from multiple concurrent threads.*

The simplest way would be to simply lock on the instance variable `item` or any dedicated object in all methods interacting with it. Another way would be to make all such methods `synchronized`.

## 2
*Discuss how well the thread-safe version of the class is likely scale if a large number of threads call get, add and set concurrently?*

The class would not scale well, since locking is basically serializing parallel execution. Indeed, each thread takes ownership of the same lock regardless of which method they call on `DoubleArrayList` instances. Therefore this solution is not ideal performance, but it is thread-safe and simplistic.

## 3
*Would this achieve thread-safety? Explain why not. Would it achieve visibility? Explain why not.*

No it would not be thrad-safe: in fact it will only be mutual exclusive on single methods, but not regarding to the monitor as a whole. For instance, if the array is resized as a result of a call to `add()`, `toString()` form another thread might output undefined values, since the first thread has not yet completed updating the array.

# Exercise 1.7
## 1
*Explain how one can make the class thread-safe enough so that the totalSize field is maintained correctly even if multiple concurrent threads work on multiple DoubleArrayList instances at the same time.You may ignore the allLists field for now.*

`totalSize` could be made `volatile`, and the statement `totalSize++` in `add()` must be made atomic by synchronizing it on the metaclass insrtance.

## 2
*Explain how one can make the class thread-safe enough so that the allLists field is maintained correctly even if multiple concurrent threads create new DoubleArrayList instances at the same time.*

Although instances of `DoubleArrayLists` are thread-safe, the static field `allLists` is not, since `HashSet` by itself is not a concurrency-safe class.
The locking must happen on a static object, such as `allLists` itself, since
locks on instance variable would be on different objects for every instance.

# Exercise 1.8
## 1
*Explain why after 10 million calls to MysteryB.increment() and 10 million concurrent calls to MysteryB.increment4(), the resulting value of count is rarely the expected 50,000,000*

`synchronized` static methods lock on the metaclass instance of the class they belong to. `MysteryB` is accessing count in `MysteryA` through inheritance, but it is still a different class, which means that it does not lock on the same object as `MysteryA`. Therefore, the methods are not mutually exclusive.

## 2
*Explain how one can use an explicit lock object and synchronized statements (not synchronized methods) to change the locking scheme, so that the result is always the expected 50,000,000.*

By explicitly specifying the object to be used for locking it is possible to have all the methods lock on the same object, hence achieving mutual exclusion. One possibility is to have a dedicated `protected` object in `MysteryA`, or to have all subclasses lock on the superclass metaclass instance, that is `MysteryA`.
