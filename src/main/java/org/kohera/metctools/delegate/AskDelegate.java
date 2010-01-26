package org.kohera.metctools.delegate;

import org.kohera.metctools.DelegatorStrategy;
import org.marketcetera.event.AskEvent;

/**
 * Delegate interface for AskEvents.
 * 
 * @author Jake Brukhman
 *
 */
public interface AskDelegate {
	
	/**
	 * 
	 * Implement this method to handle AskEvents.
	 * 
	 * @param sender
	 * @param askEvent
	 */
	public void onAsk( DelegatorStrategy sender, AskEvent askEvent );

}
