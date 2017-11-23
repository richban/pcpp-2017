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
	
