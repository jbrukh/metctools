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
	
	public void onAsk( DelegatorStrategy sender, AskEvent askEvent );


}
