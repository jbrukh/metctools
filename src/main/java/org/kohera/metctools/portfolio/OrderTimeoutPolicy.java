package org.kohera.metctools.portfolio;

import org.kohera.metctools.AdvancedStrategy;
import org.marketcetera.trade.OrderID;

public interface OrderTimeoutPolicy {
	
	public void onOrderTimeout( AdvancedStrategy sender, OrderID orderId, long timeout );

}
