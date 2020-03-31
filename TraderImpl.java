
import java.util.concurrent.locks.ReentrantLock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TraderImpl implements Trader {

	private Order inventory;
	private Grain specialGrain;
	private int maxWaitTime = 55; // millisegundos

	private final ReentrantLock orderLock = new ReentrantLock(true);
	private final ReentrantLock inventoryLock = new ReentrantLock(true);
	// locks para manejar threads/brewers que esperan por más tiempo

	public TraderImpl(Grain g) {
		this.specialGrain = g;
		inventory = new Order();
	}

	@Override
	public Order getAmountOnHand() {

		String traderSpecial = specialGrain.toString() + " TRADER HAS:";
		String cornAmount = "Corn: " + inventory.get(Grain.CORN);
		String barleyAmount = "Barley: " + inventory.get(Grain.BARLEY);
		String riceAmount = "Rice: " + inventory.get(Grain.RICE);
		String wheatAmount = "Wheat: " + inventory.get(Grain.WHEAT);

		System.out.println(traderSpecial);
		System.out.println(cornAmount + " || " + barleyAmount + " || " + riceAmount + " || " + wheatAmount);

		return inventory;
	}

	@Override
	public void get(Order order) throws InterruptedException {

		while (orderLock.isLocked()) {
			Thread.sleep(100);
		}
		// prevent incoming orders to keep organization
		orderLock.lock();
		
		try {

			int waitIteration = 0;
			while (inventory.get(this.specialGrain) < order.get(this.specialGrain)) {

				Thread.sleep(50);
				if (waitIteration++ > maxWaitTime) {
					Thread.currentThread().interrupt(); //interrupts order if it takes too long
					Thread.currentThread().join();
					return;
				}
			}

			List<Grain> gList = Arrays.asList(Grain.values());
			ArrayList<Grain> grains = new ArrayList<Grain>(gList);
			grains.remove(this.specialGrain);
			System.out.println(inventory);

			for (Grain grain : grains) {

				waitIteration = 0;
				int amt = order.get(grain) - inventory.get(grain);
				System.out.println(amt);
				boolean granFlag = true;

				while (inventory.get(grain) < order.get(grain) && granFlag) {

					if (inventory.get(specialGrain) >= amt) {

						// swaps specialty grain for gran needed to complete order
						P2.specialist(grain).swap(specialGrain, amt);
						updateInventory(grain, amt);
						updateInventory(specialGrain, -amt);
						granFlag = false;
						
					} else {

						Thread.sleep(100);
						if (waitIteration++ > maxWaitTime) {
							Thread.currentThread().interrupt();
							Thread.currentThread().join();
							return;
						}
					}
				}

			}

			// Trader's inventory stores the last transaction because the system terminates
			// the program before it can be registered
			grains.add(specialGrain);

			for (Grain grain : grains) {
				updateInventory(grain, -order.get(grain));
				//updates this traders inventory
			}
		} finally {
			orderLock.unlock();
		}

	}

	@Override
	public void swap(Grain what, int amt) throws InterruptedException {

		// order more trader's specialty grain if it has less that the amount requested,
		// wait for supplier
		int waitIteration = 0;
		while (inventory.get(this.specialGrain) < amt) {
			
			Thread.sleep(50);
			
			if (waitIteration++ > maxWaitTime) {
				Thread.currentThread().interrupt();
				Thread.currentThread().join();
				return;
			}
		}
		
		updateInventory(this.specialGrain, -amt);
		updateInventory(what, amt);
		// System.out.println("Transaction SWAP completed");

	}

	@Override
	public void deliver(int amt) throws InterruptedException {
		updateInventory(this.specialGrain, amt);

	}

	public void updateInventory(Grain grain, int i) {
		// prevent access to inventory
		inventoryLock.lock();
		try {
			// System.out.println("updateInventory: " + grain.toString() + ", amt: " + i);
			inventory.change(grain, i);
		} finally {
			inventoryLock.unlock();
		}

	}

}
