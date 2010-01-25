package org.kohera.metctools.portfolio;

import java.util.Random;

public class Test {

	private final static Object lock = new Object();
	
	synchronized public void sendOrder(final int id) {
		
		
		
		System.out.println("Sending order " + id + "...");
		Thread t = new Thread() {
			public void run() {
				System.out.println("Filling order " + id );
				try {
					Thread.sleep((new Random().nextInt(5)+2)*1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				acceptReport(id);
			}
		};
		t.start();
		synchronized(lock) {
		try {
			lock.wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }
	}
	
	public void acceptReport(int id) {
		System.out.println("Filled " + id);
		synchronized(lock) {
			lock.notify();
		}
	}
	
	public void go() throws InterruptedException {
		new Thread() {
			public void run() {
				sendOrder(101);
			}
		}.start();
		for ( int i = 1; i < 10; i++ ) sendOrder(i);
	}
	
	
	public static void main( String[] args ) throws InterruptedException {
		Test t = new Test();
		t.go();
	}
	
}
