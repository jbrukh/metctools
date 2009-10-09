package org.kohera.metctools.delegate;

import org.kohera.metctools.DelegatorStrategy;
import org.marketcetera.trade.OrderCancelReject;

public interface OrderCancelRejectDelegate extends EventDelegate {

	public void onCancelReject( DelegatorStrategy sender, OrderCancelReject reject );
	
}
