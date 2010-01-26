package org.kohera.metctools.delegate;

import org.kohera.metctools.DelegatorStrategy;

/**
 * Delegate for Strategy end events.
 * 
 * @author Jake Brukhman
 *
 */
public interface StopDelegate extends EventDelegate {

	/**
	 * 
	 * Implement this method to handle the Strategy end.
	 * 
	 * @param sender
	 */
	public void onStop( DelegatorStrategy sender );
	
}
