package org.kohera.metctools.delegate;

import org.kohera.metctools.AdvancedStrategy;
import org.marketcetera.event.TradeEvent;

/**
 * Delegate interface for TradeEvents.
 * 
 * @author Administrator
 *
 */
public interface TradeDelegate extends EventDelegate {
	
	public void onTrade( AdvancedStrategy sender, TradeEvent tradeEvent );
	
}
