package org.kohera.metctools.portfolio;

import org.apache.log4j.Logger;

public abstract class ProcessorThread extends Thread {
	
	private final static Logger logger =
		Logger.getLogger(ProcessorThread.class);
	private final Object lock;
	
	/**
	 * Get a new instance.
	 * 
	 * @param lock
	 */
	public ProcessorThread(final Object lock) {
		this.lock = lock;
	}
	
	/**
	 * Method that
	 */
	public abstract void action();
	
	public final void run() {
		logger.trace("--- Starting action...");
		action();
		
		logger.trace("--- Waiting for lock release...");
		
		synchronized(lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
				logger.trace("--- The processor thread is interrupted.");
				return;
			}
		}
		
		logger.trace("--- The lock is released.");
	}

}
