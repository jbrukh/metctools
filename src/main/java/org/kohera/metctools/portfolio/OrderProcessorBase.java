package org.kohera.metctools.portfolio;

import org.kohera.metctools.util.OrderBuilder;
import org.marketcetera.trade.ExecutionReport;
import org.marketcetera.trade.OrderID;
import org.marketcetera.trade.OrderSingle;

public class OrderProcessorBase {
	
	/* for locking transactions */
	private final Object transactionLock = new Object();
	private OrderID pendingOrderId;
	private PortfolioStrategy parent;
	
	/**
	 * Get a new instance.
	 */
	public OrderProcessorBase() {
		
	}

	public OrderID getPendingOrderId() {
		return pendingOrderId;
	}
	
	public void acceptExecutionReport( ExecutionReport report ) {
		
	}
	
	protected void sendOrder( OrderSingle order, long timeout, OrderTimeoutPolicy policy ) {
		
	}

}
