package org.kohera.metctools.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Generic Timer class for scheduling future tasks.
 * 
 * @author Jake Brukhman
 *
 */
public class Timer implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4343698432446376276L;
	
	/* fields */
	public List<TaskThread> threads;
	
	/**
	 * Interface for specifying generic Tasks.
	 *
	 */
	public interface Task {
		public void performTask();
	}
	
	/**
	 * Runs a Task on a separate thread after a delay.
	 * 
	 * @author Jake Brukhman
	 *
	 */
	public class TaskThread extends Thread {
		/* fields */
		private long delay;
		private Task task;
		
		public TaskThread(long delay, Task task) {
			super();
			this.delay = delay;
			this.task = task;
		}
		
		public void run() {
			try {
				Thread.sleep(delay);
				task.performTask();
	
				/* remove yourself from the list */
				threads.remove(this);
			} catch ( InterruptedException e) {
				System.err.println("interrupted " + this.toString() );
			}
		}
	}
	
	/**
	 * Get a new Timer object.
	 */
	public Timer() {
		threads = new ArrayList<TaskThread>();
	}

	/**
	 * Fire task after a delay.
	 * 
	 * If the delay is 0, then the Task is executed immediately.
	 * If the delay is negative, then the Task is never executed.
	 * 
	 * @param delay
	 * @param task
	 * @return
	 */
	public TaskThread fireIn( final long delay, final Task task ) {
		
		TaskThread thr = new TaskThread(delay,task);
		threads.add(thr);
		thr.start();
		return thr;
	}
	
	/**
	 * Fire task at specific date and time.
	 * 
	 * @param date
	 * @param task
	 * @return
	 */
	public TaskThread fireAt( final Date date, final Task task ) {
		long now = new Date().getTime();
		long then = date.getTime();
		long delay = Math.max(then-now, 0);
		return fireIn(delay,task);
	}
	
	/**
	 * 
	 * Kill a task in this Timer object.
	 * 
	 * @param taskThr
	 */
	public void kill( TaskThread taskThr) {
		int index = threads.indexOf(taskThr);
		if ( index >= 0) {
			taskThr.interrupt();
			threads.remove(index);
		}
	}
	
	/**
	 * Kill all tasks in this Timer object.
	 */
	public void killAll() {
		for ( TaskThread thr : threads ) {
			kill(thr);
		}
	}
	
}
