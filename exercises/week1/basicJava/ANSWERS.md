# Exercise 1.1

## 1.1

Count is 14193731 and should be 20000000
Count is 19796814 and should be 20000000

Threads  t1 and t2 could call increment at the same time and read the same value and then both add one to it, which results in the same number

## 1.2

the count is much slower therefore the probability that t1 and t2 interleave by the runtime is also smaller.

## 1.3

It doesn't make any change it's just an syntactic sugar

## 1.4

In order to obtain the right results (0) we have to use the synchronized which ensures that t1 and t2 threads are coordinated and do not interfere with one another.

## 1.5
