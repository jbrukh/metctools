package org.kohera.metctools.delegate;

import org.kohera.metctools.DelegatorStrategy;

/**
 * Delegate for Strategy start events.
 * 
 * @author Jake Brukhman
 *
 */
public interface StartDelegate extends EventDelegate {

	/**
	 * 
	 * Implement this method to handle the Strategy start.
	 * 
	 * @param sender
	 */
	public void onStart( DelegatorStrategy sender);
	
}
