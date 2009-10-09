package org.kohera.metctools.delegate;

import org.kohera.metctools.DelegatorStrategy;
import org.marketcetera.event.TradeEvent;

/**
 * Delegate interface for TradeEvents.
 * 
 * @author Administrator
 *
 */
public interface TradeDelegate extends EventDelegate {
	
	public void onTrade( DelegatorStrategy sender, TradeEvent tradeEvent );
	
}
