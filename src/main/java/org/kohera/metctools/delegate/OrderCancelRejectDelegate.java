package org.kohera.metctools.delegate;

import org.kohera.metctools.AdvancedStrategy;
import org.marketcetera.trade.OrderCancelReject;

public interface OrderCancelRejectDelegate extends EventDelegate {

	public void onCancelReject( AdvancedStrategy sender, OrderCancelReject reject );
	
}
