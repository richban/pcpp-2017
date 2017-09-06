# Exercise 1.3

## 1
Yes, I do. This is due to the reading thread accessing its cached value of MutableInteger, which is never updated by the main thread.

## 2
Yes, it does. Indeed, `synchronized` implies visibility, which is out ultimate problem.

## 3
No, it doesn't. This is due to the lack of visibility of the value in the reading phase.
It might happen that, due to the way threads are scheduled, the value is set before the other thread first reads it: this leads to a correct execution of the program, but is absolutely casual and not to be relied upon.
Even though there are no issues strictly related to concurrency when reading, the Java impolementation on modern computer architectures creates other problems, in particular regarding multiple levels of cache memory: in fact, the default behavior is to operate on cache memory as much as possible, while the shared-memory concurrency model requires all threads to use the same memory. This goal can be achieved by using either the `volatile` or `synchronized` keywords, the latter assuring also atomicity.

## 4
Yes, it does. The `volatile` keyword alone is enough in this case, because there is no need for atomicity, which is granted by `synchronized`: all write operations on the shared resource don't require reading any shared resource first, which makes atomicity redundant.

# 3
## 1
No, it's not. These are the results:
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
The Mystery class is not thread-safe because of the difference in the locking mechanism for instance method and static ones: the former block on the instance they are called on, while the latter on the instance of the metaclass for the class they belong to. Therefore, by locking on two different objects, mutual exclusion is faulty.

## 3
Mystery class has been made thread-safe by having the addInstance() methos acquire the lock of the same object as the static methods, that is Mystery.class. The oposite is not possible, since static methods cannot access the instance object. Following, the results.
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
