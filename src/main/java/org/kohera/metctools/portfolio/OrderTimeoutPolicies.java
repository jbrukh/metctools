package org.kohera.metctools.portfolio;

import org.apache.log4j.Logger;
import org.kohera.metctools.DelegatorStrategy;
import org.marketcetera.trade.OrderID;

/**
 * Various pre-programmed OrderTimeoutPolicy objects.
 * 
 * @author Jake Brukhman
 *
 */
public final class OrderTimeoutPolicies {
	
	/**
	 * Sends a WARN LogEvent to the parent strategy to let it know that
	 * an order timeout has occurred.
	 * 
	 */
	public final static OrderTimeoutPolicy ON_TIMEOUT_WARN = new OrderTimeoutPolicy() {
		public void onOrderTimeout(DelegatorStrategy sender, OrderID orderId, long timeout, Trade trade) {
			Logger.getLogger(PortfolioStrategy.class)
			  .warn(">>>\tCould not fill order in " + timeout + " ms. (Ignoring)");
		}		
	};
	
	/**
	 * Like ON_TIMEOUT_WARN, but also attempts to cancel the order in question
	 * without checking for success.
	 * 
	 */
	public final static OrderTimeoutPolicy ON_TIMEOUT_CANCEL = new OrderTimeoutPolicy() {
		public void onOrderTimeout(DelegatorStrategy sender, OrderID orderId, long timeout, Trade trade) {
			Logger.getLogger(PortfolioStrategy.class)
			  .warn(">>>\tCould not fill order in " + timeout + " ms. (Canceling)");
			trade.order().cancelOrder();
		}		
	};

}
