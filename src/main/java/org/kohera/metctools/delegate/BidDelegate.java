package org.kohera.metctools.delegate;

import org.kohera.metctools.DelegatorStrategy;
import org.marketcetera.event.BidEvent;

/**
 * Delegate interface for BidEvents.
 * 
 * @author Jake Brukhman
 *
 */
public interface BidDelegate {
	
	/**
	 * 
	 * Implement this method to handle BidEvents.
	 * 
	 * @param sender
	 * @param bidEvent
	 */
	public void onBid( DelegatorStrategy sender, BidEvent bidEvent );
	
}
