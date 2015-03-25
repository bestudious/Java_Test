package cn.dlstone.TestThread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

//生产者和消费者
class Meal {
	private final int orderNum;
	
	public Meal(int orderNum) {
		this.orderNum = orderNum;
	}
	
	public String toString() {
		return "Meal " + orderNum;
	}
}

class Waitperson implements Runnable {
	private Restaurant restaurant;
	
	public Waitperson(Restaurant r) {
		restaurant = r;
	}
	
	@Override
	public void run() {
		try {
			while(!Thread.interrupted()) {
				synchronized (this) {
					while(restaurant.meal == null) {
						this.wait();
					}
				}
				System.out.println("Waitperson got" + restaurant.meal);
				synchronized (restaurant.chef) {
					restaurant.meal = null;
					restaurant.chef.notifyAll();
				}
			}
		} catch (InterruptedException e) {
			System.out.println("WaitPerson interrupted");
		}
		
	}
	
}

class Chef implements Runnable {
	private Restaurant restaurant;
	private int count = 0;
	public Chef(Restaurant r) {
		restaurant = r;
	}
	
	@Override
	public void run() {
		try {
			while(!Thread.interrupted()) {
				synchronized (this) {
					while (restaurant.meal != null) {
						this.wait();
					}
					if(++count == 10) {
						System.out.println("Out of food, closing");
						restaurant.exec.shutdownNow();
					}
					System.out.println("Order up!");
					synchronized (restaurant.waitperson) {
						restaurant.meal = new Meal(count);
						restaurant.waitperson.notifyAll();
					}
					TimeUnit.MILLISECONDS.sleep(100);
				}
			}
		} catch (InterruptedException e) {
			System.out.println("Chef interrupted");
		}
		
	}
	
}

public class Restaurant {
	Meal meal;
	ExecutorService exec = Executors.newCachedThreadPool();
	Waitperson waitperson = new Waitperson(this);
	Chef chef = new Chef(this);
	public Restaurant() {
		exec.execute(chef);
		exec.execute(waitperson);
	}

	public static void main(String[] args) {
		new Restaurant();
	}

}
