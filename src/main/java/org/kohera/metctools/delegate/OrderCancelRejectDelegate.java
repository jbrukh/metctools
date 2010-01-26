package org.kohera.metctools.delegate;

import org.kohera.metctools.DelegatorStrategy;
import org.marketcetera.trade.OrderCancelReject;

/**
 * OrderCancelRejectDelegate interface.
 * 
 * @author Jake Brukhman
 *
 */
public interface OrderCancelRejectDelegate extends EventDelegate {

	/**
	 * 
	 * Override this method to handle OrderCancelRejects.
	 * 
	 * @param sender
	 * @param reject
	 */
	public void onCancelReject( DelegatorStrategy sender, OrderCancelReject reject );
	
}
