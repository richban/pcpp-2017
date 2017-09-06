# Exercise 1.3

## 1
Yes, the `t` thread hangs. This is because `t` is accessing the instance of `MutableInteger` in the cache memory of the CPU core it is running on, which is not the same instance updated by the main thread.

## 2
Yes, the program executes as expected. Indeed, the `synchronized` keyward in Java not only ensures mutual exclusion, but also guarantees visibility, that is the values are read and written from and to the main memory.

## 3
No, the `t` thread hangs again. This is due to the lack of visibility of the value stored in the `MutableInteger` instance when it is read.
However, in some cases the program still terminates correctly: in fact, it might happen that, due to the way threads are scheduled, the value is set before the other thread first reads it. Nevertheless, this phenomenon is absolutely casual and not to be relied upon.
This bug arised when the `synchronized` keyword was removed: in fact, in addition to mutual exclusion, it also guarantees visibility.

## 4
Yes, the program executes as expected again. Indeed, the `sychronized` keyword is redundant in this case, because the bug is caused by lack of visibility, and not by absence of atomicity: in fact, all write operations on the shared resource don't require reading any shared resource first. This means that the `volatile` keyword alone is sufficient to solve it.

# 3
## 1
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
The Mystery class is not thread-safe because of the difference in the locking mechanism for instance method and static ones: the former block on the instance they are called on, while the latter on the instance of the metaclass for the class they belong to. Therefore, because the mutual exclusion happpens on two different objects, mutual exclusion is faulty.

## 3
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
