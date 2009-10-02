package org.kohera.metctools.delegate;

import org.kohera.metctools.AdvancedStrategy;
import org.marketcetera.event.BidEvent;

/**
 * Delegate interface for BidEvents.
 * @author Administrator
 *
 */
public interface BidDelegate {
	
	public void onBid( AdvancedStrategy sender, BidEvent bidEvent );
	
}
