package org.kohera.metctools.delegate;

import org.kohera.metctools.DelegatorStrategy;
import org.marketcetera.event.BidEvent;

/**
 * Delegate interface for BidEvents.
 * @author Administrator
 *
 */
public interface BidDelegate {
	
	public void onBid( DelegatorStrategy sender, BidEvent bidEvent );
	
}
