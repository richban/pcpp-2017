## ExecutorService

Different task may run different threads, so objects (clusters) accessed by
tasks must be thread-safe. In KPMean1P ```besst.add(p)``` is not thread-safe.

For a *Void* method (different from a *void* method), you have to return null.

*Void* is just a placeholder stating that you don't actually have a return value
(even though the construct -- like Callable here -- needs one). The compiler does
not treat it in any special way, so you still have to put in a "normal" return statement yourself.


## Exercise 2

Assignment step is thread-safe. We just storing *best* cluster reference
to *myCluster[pi]*, therefore there is no collision of threads.
