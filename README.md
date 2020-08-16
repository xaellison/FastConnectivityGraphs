# FastConnectivityGraphs

Written for a tech interview in 2016. A graph data structure which allows for constant time checks if two vertices have a path connecting them (equivalently, that they're in the same component).


# Running
Currently the main method awaits standard input. For testing performance, you can un-comment out `randomizedTest();` in `main`.  

Compile:
`javac FastConnectivityGraphs.java`

Run:
```
alex$ java FastConnectivityGraph
add 1
add 2
is linked 1 2
false
add 1 2
is linked 1 2
true
add 3 4
is linked 3 4
true
is linked 2 3
false
```
