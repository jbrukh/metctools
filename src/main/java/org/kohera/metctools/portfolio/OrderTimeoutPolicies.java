package org.kohera.metctools.portfolio;

import org.kohera.metctools.DelegatorStrategy;
import org.marketcetera.trade.OrderID;

public final class OrderTimeoutPolicies {
		
	public final static OrderTimeoutPolicy ON_TIMEOUT_WARN = new OrderTimeoutPolicy() {
		public void onOrderTimeout(DelegatorStrategy sender, OrderID orderId, long timeout, ITrade trade) {
			sender.getRelay().warn("Could not fill order in " + timeout + " ms. (Ignoring)");
		}		
	};
	
	public final static OrderTimeoutPolicy ON_TIMEOUT_CANCEL = new OrderTimeoutPolicy() {
		public void onOrderTimeout(DelegatorStrategy sender, OrderID orderId, long timeout, ITrade trade) {
			sender.getRelay().warn("Could not fill order in " + timeout + " ms. (Canceling)");
			sender.getRelay().cancelOrder(orderId);
		}		
	};

}
