
###### Mark7
After a certain iterations we can assume that the optimizations performed by
JIT doesn't increase and does not produce new machine code which is eventually
cached and therefore the runtime eventually stabilize. However these results are
influenced by other external processes.

```
# OS:   Mac OS X; 10.12.6; x86_64
# JVM:  Oracle Corporation; 1.8.0_144
# CPU:  null; 8 "cores"
# Date: 2017-09-28T19:15:44+0200
pow                                  76.7 ns       0.87    4194304
exp                                  55.1 ns       0.56    8388608
log                                  25.3 ns       0.37   16777216
sin                                 121.7 ns       1.85    2097152
cos                                 121.4 ns       1.30    2097152
tan                                 158.9 ns       5.70    2097152
asin                                228.4 ns       2.11    2097152
acos                                212.6 ns       2.20    2097152
atan                                 52.0 ns       0.50    8388608
```

## 4.2

###### Mark6
Creating an objects in Java took us around 70 ns. While creating a Thread took us around 898.8 ns. Altought
Java defines Threads as objects they're in reallity more complex than a ordinary objects which are only available
for the JVM and require a fair bit more
work involved.

###### Mark7
Unlike Java Objects, thread (objects) creation is expensive because under the hood there is a bit of work
especially registration with the host OS. Altough starting a thread takes up much more time because the OS has
to manage and schedule the threads. The actual work took about  52887.8 ns which heavly depends on the
`.join()` which is waiting idle for the other thread to finish.

```
# OS:   Mac OS X; 10.12.6; x86_64
# JVM:  Oracle Corporation; 1.8.0_144
# CPU:  null; 8 "cores"
# Date: 2017-09-28T20:26:07+0200
hashCode()                            2.7 ns       0.04  134217728
Point creation                       74.6 ns       1.13    4194304
Thread's work                      5765.2 ns      83.96      65536
Thread create                       883.5 ns       5.72     524288
Thread create start               38290.5 ns     132.28       8192
Thread create start join          52887.8 ns     584.81       8192
ai value = 1638340000
Uncontended lock                      5.5 ns       0.06   67108864
```
>>>>>>> db9b8c96acc7a6b83e68642f1ab45fa332956fb3
