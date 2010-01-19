package org.kohera.metctools.portfolio;

import java.io.Serializable;

import org.kohera.metctools.DelegatorStrategy;
import org.marketcetera.trade.OrderID;

public interface FillPolicy extends Serializable {
	
	public void onFill( DelegatorStrategy sender, OrderID orderId, Trade trade );

}
