package org.kohera.metctools.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Timer {
	
	public List<TaskThread> threads;
	
	public interface Task {
		public void performTask();
	}
	
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
	public TaskThread fireAt( final Date date, final Task task) {
		long now = new Date().getTime();
		long then = date.getTime();
		long delay = Math.min(then-now, 0);
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
	
	
	public static void main( String[] args ) throws InterruptedException {
		Timer t = new Timer();
		
		TaskThread thr = t.fireIn(60*1000, new Task() {

			@Override
			public void performTask() {
				System.out.println("task");
			}
		 
		});
		
		//Thread.sleep(3000);
		t.kill(thr);
		System.out.println(t.threads.size());
		
	}
		
}
