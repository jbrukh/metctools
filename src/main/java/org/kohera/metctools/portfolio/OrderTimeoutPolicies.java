package org.kohera.metctools.portfolio;

import org.apache.log4j.Logger;
import org.kohera.metctools.DelegatorStrategy;
import org.kohera.metctools.Messages;
import org.marketcetera.trade.OrderID;


/**
 * Various pre-programmed OrderTimeoutPolicy objects.
 * 
 * @author Jake Brukhman
 *
 */
@SuppressWarnings("serial")
public final class OrderTimeoutPolicies {
	
	/**
	 * Sends a WARN LogEvent to the parent strategy to let it know that
	 * an order timeout has occurred.
	 * 
	 */
	public final static OrderTimeoutPolicy ON_TIMEOUT_WARN = new OrderTimeoutPolicy() {
		
		@Override
		public void onOrderTimeout(DelegatorStrategy sender, OrderID orderId, 
				long timeout, Trade trade) {
		
			/* default message */
			Logger.getLogger(PortfolioStrategy.class).warn(
					Messages.MSG_ON_TIMEOUT_WARN(timeout)
					);
		
		}		
	};
	
	/**
	 * Like ON_TIMEOUT_WARN, but also attempts to cancel the order in question
	 * without checking for success.
	 * 
	 */
	public final static OrderTimeoutPolicy ON_TIMEOUT_CANCEL = new OrderTimeoutPolicy() {
		
		@Override
		public void onOrderTimeout(DelegatorStrategy sender, OrderID orderId, 
				long timeout, Trade trade) {
			
			/* log it... */
			Logger.getLogger(PortfolioStrategy.class).warn(
					Messages.MSG_ON_TIMEOUT_CANCEL(timeout)
					);
			
			/* ...and cancel the order */
			trade.order().cancelOrder();
		}		
	
	};

}
