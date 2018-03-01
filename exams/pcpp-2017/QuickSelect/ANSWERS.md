#Q1 - finding the median - Quickselect

### 1

graph


### 2

*Implementation* ```TestQuickSelect.java```

I have parallelized ```quickCountRec()```  see the ```quickCountParRec()``` method.
I have used tasks, as the exam outlined I divide the array equally into regions ```Ai```
each task get's a portion of the array and counts the number ```si``` of elements which
are smaller then the pivot in the region ```Ai```. The ```counter``` is local to every thread,
therefore there is no need for synchronization and also there is no shared mutate state,
since all threads are working on a different region of ```Ai```. Afterwards I sum up all the
local count of each thread which than tells which direction we need to filter.

Given this information, similarly the filtering of the regions is divided into different tasks.
Because of the indexing of regions and for the sake of the implementation simplicity, each
tasks has it's own local array where the filtering is happening and in the end each local
array is concatenated in order. However this might not be an optimal solution and a more clever
solution could be implemented by keeping the counts of each region at the counting stage.
