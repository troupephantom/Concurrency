# Used for Low Latency System 

## Problem : 
Any Producer Consumer Problem (consider trade system where data coming from exchange)

## Blocking Queue : 
We can use Blocking for Producer Consumer , before every data inserted lock is acquired and released after storing the data 
which make sure if hit capacity no new data is added , while on consumer side lock make sure when queue is empty no data 
consumed , for that it maintains pointer head and tail 

## What is the Problem ? 
In Low Latency Systems , Thread lock increases latency and every time data is added it creates an object on runtime 
which makes GC pause increasing latency , also fir every different consumer we need different blocking queue 

## To reduce Latency we can use Disrupter Queue : 
Core Idea ; Create an array of fixed size (power of 2) ( Ring Buffer ) , take two pointer publisher and consumer sequence 
during init of service it initialize the object and added on the array for every index, while adding the data we just modify the 
data prsent on index makes it low latency as no GC pause just modify data. Also increasing sequence linear , not % mod after 
modification this helps to know which consumer consume which data
Few Constraint : Use Thread wait when consumer seq == producer seq && (producer seq - consumerseq > capacity) 

