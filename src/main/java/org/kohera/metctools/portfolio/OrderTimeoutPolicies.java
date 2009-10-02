package org.kohera.metctools.portfolio;

import org.kohera.metctools.AdvancedStrategy;
import org.marketcetera.trade.OrderID;

public final class OrderTimeoutPolicies {
		
	public final static OrderTimeoutPolicy ON_TIMEOUT_WARN = new OrderTimeoutPolicy() {
		public void onOrderTimeout(AdvancedStrategy sender, OrderID orderId, long timeout) {
			sender.getRelay().warn("Could not fill order in " + timeout + " ms. (Ignoring)");
		}		
	};
	
	public final static OrderTimeoutPolicy ON_TIMEOUT_CANCEL = new OrderTimeoutPolicy() {
		public void onOrderTimeout(AdvancedStrategy sender, OrderID orderId, long timeout) {
			sender.getRelay().warn("Could not fill order in " + timeout + " ms. (Canceling)");
			sender.getRelay().cancelOrder(orderId);
		}		
	};

}
