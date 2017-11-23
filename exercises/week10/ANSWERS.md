Group: 23

*Name 1:* Patrick Evers Bjoerkman (pebj@itu.dk)
*Name 2:* Davide Laezza (dlae@itu.dk)
*Name 3:* Richard Banyi (riba@itu.dk)

# Exercise 10.1

## Task 1

    See source code CasHistogram.java

## Task 2
```
**********
   0:         2
   1:    283146
   2:    790986
   3:    988651
   4:    810386
   5:    524171
   6:    296702
   7:    155475
   8:     78002
   9:     38069
  10:     18232
  11:      8656
  12:      4055
  13:      1886
  14:       865
  15:       400
  16:       179
  17:        79
  18:        35
  19:        14
  20:         7
  21:         2
  22:         0
  23:         0
  24:         0
  25:         0
  26:         0
  27:         0
  28:         0
  29:         0
        4000000
```
## Task 3

class CasHistogram-->50006.142326087
class StmHistogram-->50007.569116063
class CasHistogram-->50008.568982462
class StmHistogram-->50009.756693932

# Exercise 10.2

## Task 1-6
	See source code Exercise10.2.java

## Task 7
	See source code Exercise10.2.java, testing code based on week8->TestStripedWriteMap.java->parallelTest.

## Task 8
	** Explain why such a solution would work in this particular case, even if the test-then-set sequence is not atomic.
	The Holders object is an immutable linked list datastructure. The object where contains is called therefore won't change and
	it will therefore work to simply use contains in the given context of this assignment.
	** DAVIDE I DONT UNDERSTAND,,, USE YOUR DIVINE KNOWLEDGE

## Exercise 10.3
The test compares different versions of concurrent random integer generators,
crossing two different criteria: lock- vs CAS-based implementations and shared
vs thread-local instance. The table below summarizes such criteria. There is
also a fifth class, `WrappedTLRandom`, which is a simple wrapper around Java
standard library `ThreadLocalRandom`.

|        |      lock       |     CAS     |
|:------:|:---------------:|:-----------:|
| Shared | LockingRandom   | CasRandom   |
| Local  | TLLockingRandom | TLCasRandom |

The expectations are for lock-based implementations to be faster than their
CAS-based counterparts in high-contention situations, while the latter should
be more rapid in the opposite situation, that is low-contention. Regardless of
the implementation, shared instances are foreseen to be a bottleneck, thus
making thread-local generators more efficient. Finally, `WrappedTLRandom` is
forecast to be the quickest of all five alternatives: even though its innards
are not known, it most likely  relies on hardware-implemented CAS, which makes
it incredibly fast on CPUs implementing optimally such instruction. Nonetheless,
as the previous assignments have shown us, in these simple cases naive
implementations are often more performing.

The following table summarizes the data for the experiment: figures for a number
of threads less than 15 are from tests on a 4-core CPU, while the rest are taken
from tests on a 16-cores CPU

|      Class      | #Threads |     Mean    | STD deviation |
|:---------------:|:--------:|:-----------:|:-------------:|
| LockingRandom   |    1     | 16772017 s  |    5515927    |
| CasRandom       |    1     | 14756879 s  |    2475554    |
| TLLockingRandom |    1     | 27827929 s  |    9840360    |
| TLCasRandom     |    1     | 15140627 s  |    9600783    |
| WrappedTLRandom |    1     | 6165232  s  |    5157838    |

| LockingRandom   |    2     | 77727667 s  |    8302449    |
| CasRandom       |    2     | 84950581 s  |    22214078   |
| TLLockingRandom |    2     | 6909804 s   |    853757     |
| TLCasRandom     |    2     | 9105039 s   |    1628999    |
| WrappedTLRandom |    2     | 3834175 s   |    567568     |

| LockingRandom   |    4     | 52399810 s  |    6674042    |
| CasRandom       |    4     | 62959059 s  |    36056335   |
| TLLockingRandom |    4     | 12082326 s  |    1764131    |
| TLCasRandom     |    4     | 13948427 s  |    1783922    |
| WrappedTLRandom |    4     | 8536496 s   |    2557941    |

| LockingRandom   |    8     | 49107355 s  |    3311549    |
| CasRandom       |    8     | 113182890 s |    27930950   |
| TLLockingRandom |    8     | 7846313 s   |    2148737    |
| TLCasRandom     |    8     | 8155158 s   |    725339     |
| WrappedTLRandom |    8     | 4026803 s   |    583621     |

| LockingRandom   |    15    | 49097134 s  |    2806304    |
| CasRandom       |    15    | 130031804 s |    7643925    |
| TLLockingRandom |    15    | 7574348 s   |    484885     |
| TLCasRandom     |    15    | 8311587 s   |    517294     |
| WrappedTLRandom |    15    | 4810916 s   |    487412     |

| LockingRandom   |    16    | 53427157 s  |    272477     |
| CasRandom       |    16    | 149766289 s |    6632706    |
| TLLockingRandom |    16    | 3752512 s   |    432722     |
| TLCasRandom     |    16    | 4026952 s   |    399788     |
| WrappedTLRandom |    16    | 2523123 s   |    251227     |

| LockingRandom   |    24    | 54833932 s  |    713824     |
| CasRandom       |    24    | 183167552 s |    10135858   |
| TLLockingRandom |    24    | 3921562 s   |    386372     |
| TLCasRandom     |    24    | 4229818 s   |    352897     |
| WrappedTLRandom |    24    | 2660956 s   |    307790     |

| LockingRandom   |    32    | 55909929 s  |    733466     |
| CasRandom       |    32    | 169175168 s |    2965232    |
| TLLockingRandom |    32    | 4299468 s   |    236862     |
| TLCasRandom     |    32    | 4530463 s   |    275494     |
| WrappedTLRandom |    32    | 2883834 s   |    288608     |

`LockingRandom` performs much faster in the single-thread situation, when
no actual concurrency is happening, its execution times increasing
significantly as soon as more than one thread is involved and stabilizing
when the maximum degree of parallelism allowed by the hardware is reached.
`CasRandom`, on the contrary, shows an increased execution time according
with a rise in the number of threads involved. This confirms our hypotheses
about its worsening performances in high-contention scenarios.

`TLLockingRandom` and `TLCasRandom` exhibit the same pattern of decreasing the
execution time as the number of thread increase, indicating a better thread
scalability, definitely owing to the locality of memory.

`WrappedTLRandom` can keep up with high concurrency, being quicker when there
are more threads executing that available cores than in the reverse case.

In conclusion, our expectations are fulfilled: `WrappedTLRandom` is by far
the best performing class. As soon as a minimum level of concurrency is
involved, the thread-local implementations are much faster than shared ones,
and CAS-based implementations can not deal with high-contention situations
as well as lock-based ones.
