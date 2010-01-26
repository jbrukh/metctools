package org.kohera.metctools.delegate;

import org.kohera.metctools.DelegatorStrategy;
import org.marketcetera.event.TradeEvent;

/**
 * Delegate interface for TradeEvents.
 * 
 * @author Jake Brukhman
 *
 */
public interface TradeDelegate extends EventDelegate {
	
	/**
	 * 
	 * Implement this method to handle TradeEvents.
	 * 
	 * @param sender
	 * @param tradeEvent
	 */
	public void onTrade( DelegatorStrategy sender, TradeEvent tradeEvent );
	
}
