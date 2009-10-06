package org.kohera.metctools.portfolio;

import org.kohera.metctools.DelegatorStrategy;
import org.marketcetera.trade.OrderID;

public interface OrderTimeoutPolicy {
	
	public void onOrderTimeout( DelegatorStrategy sender, OrderID orderId, long timeout, ITrade trade );

}
