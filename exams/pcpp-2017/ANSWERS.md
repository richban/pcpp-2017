# Question 6

```
volatile int state = -1;
int consensus(int x) { // invariant: x > 0
  assert( x > 0 ) ;
  if( state > 0 )
     return state;
  else
state = x;
     return state;
}
```

### 6.1
* Describe a situation where the above does not achieve consensus.


### 6.2
* Now consider the variant where the consensus method is declared synchronized. Which requirement for consensus protocols is violated by this solution? Give an example execution.

### 6.3

* Consider the following variant:

```
AtomicInteger state = new AtomicInteger(-1);
int consensus(int x) { // invariant: x > 0
    assert( x > 0 ) ;
    if( state.get() > 0 ) return state;
    while( true ) {
        if( state.compareAndSet(-1,x) ) return state.get();
        }   
    }
```
