
Hi I wanted to make this solution scalable and robust. Frankly, it didn't work out. I know how to fix this, but I'll add it later.

Overview:
There are Producers, Q (queue), Consumers.
Producers write to Q verified orders on correctness.
Consumers process each order separately and have a shared memory, with mapOfOrder it’s like DB, and order lists.


Why the solution is not robust with scaling:
I added locks when add in map, print values ​​and work with each order in a separate thread to avoid to decrease the same order twice, for example. The problem is that 2 consumers can process matching orders at the same time and add matching orders w / o reviewing this case. We can solve this in two ways:
- Adding orders to the list before starting work on it so that matching orders know about each other. There will be a problem that they can be found in a dead lock. Also better to add a timeout on waiting, if matched order is locked, and there are no more matched orders. For Dead lock there are some additional workarounds for this, such as a two-phase commit or adding an accidental attempt deadline lock, maybe some other solutions would work here.
- Create a new list for new orders and check for new orders for the order we are working on. It will block that order on the thread it is working on.
- Adding a mechanism for finding matching orders before printing, adding them to Q, and also for printing matching orders as a result, or hiding them if necessary.
All decisions must be coordinated with the requirements.