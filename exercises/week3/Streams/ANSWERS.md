# Answers week 3

### Exercise 3.1

*** Source file: Example154.java ***

### Exercise 3.2

*** Source file: parallelOp.java ***

This is the output of the program:

```
664579
1.0844899477790795
1.080408961485814
1.0778734863718156
1.0760825639047513
1.07515901325279
1.073907637242721
1.073235665308496
1.0724661949362
1.0719440865480065
1.0711747889618228
```

### Exercise 3.3


### Exercise 3.4

*** Source file: Stream.java ***

Results after 1st run:
```
Sum1 =  21.3004815003479420
 and it took : 9291 ms
Sum2 =  21.3004815003479420
 and it took : 1837 ms with parallel
Sum4 =  21.3004815013479420
 and it took : 5624 ms with parallel
Sum3 =  21.3004815003485500
 and it took : 16542 ms with for-loop
Sum5 =  64.3785374999245300
 and it took : 4660 ms with parallel
```

Results after 2nd run:
```
Sum1 =  21.3004815003479420
 and it took : 9176 ms
Sum2 =  21.3004815003479420
 and it took : 1810 ms with parallel
Sum4 =  21.3004815013479420
 and it took : 5519 ms with parallel
Sum3 =  21.3004815003485500
 and it took : 16836 ms with for-loop
Sum5 =  62.1185016480030400
 and it took : 4825 ms with parallel
```

For Sum5 the results are different: indeed, the generator of the stream is
accessed concurrently without any mutual exclusion, therefore the values in the
stream are not the predicted ones. As a confirmation we added the `synchronized`
keyword to the `getAsDouble` method, producing the following output:

```
Sum1 =  21.3004815003479420
 and it took : 9169 ms
Sum2 =  21.3004815003479420
 and it took : 1800 ms with parallel
Sum4 =  21.3004815013479420
 and it took : 5533 ms with parallel
Sum3 =  21.3004815003485500
 and it took : 16053 ms with for-loop
Sum5 =  21.3004815013475270
 and it took : 77291 ms with parallel
 ```
The result is correct now, but the running time has increased significantly
due to `synchronized` allowing for almost no parallelism.
