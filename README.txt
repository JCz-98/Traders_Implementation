
Project N2 -> TraderImpl

Jonathan Cazco
Andrea Porras

This solution uses locks on both the traders inventory and any incoming order from a brewers to manage threads

Implemented methods:

getAmountOnHand() -> Stock representation of each trader's final inventory, printed when the program terminates for statistics.

get(Order order) -> Manages incoming orders using a lock, once the order is complete or interrupted, it releases the lock.
It also swaps its specialty grain with another trader if inventory amount is not enough to fill the order.
Relies on updateInventory method to safely update the trader's stock.
This method also has a max waiting time that terminates the order if it takes too long to complete.

swap(Grain what, int amt) -> Swaps the grain specified with other trader capable of fulfilling the request.
This method also relies on updateInventory to manage access and changes to the current stock.

deliver(int amt) -> Delivers the amount specified.
Relies on updateInventory() for stock changes.

updateInventory(Grain grain, int i) -> Uses a lock to organize access and updates on each trader's inventory.
Once the operation is completed, releases the lock.
