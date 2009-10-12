package org.kohera.metctools.portfolio;

import org.kohera.metctools.DelegatorStrategy;
import org.marketcetera.trade.OrderID;

public interface FillPolicy {
	
	public void onFill( DelegatorStrategy sender, OrderID orderId, Trade trade );

}
