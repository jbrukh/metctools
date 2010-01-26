package org.kohera.metctools.delegate;

import org.kohera.metctools.DelegatorStrategy;

/**
 * Delegate interface for Callback events. (For completeness.)
 * 
 * @author Jake Brukhman
 *
 */
public interface CallbackDelegate extends EventDelegate {

	/**
	 * Override this method to handle Callback events.
	 * 
	 * @param sender
	 * @param message
	 */
	public void onCallback( DelegatorStrategy sender, Object message );
	
}
