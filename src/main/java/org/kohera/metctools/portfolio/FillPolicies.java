package org.kohera.metctools.portfolio;

import org.kohera.metctools.DelegatorStrategy;
import org.marketcetera.trade.OrderID;

public class FillPolicies {

	public final static FillPolicy ON_FILL_WARN = new FillPolicy() {
		@Override
		public void onFill(DelegatorStrategy sender, OrderID orderId,
				ITrade trade) {
			sender.getRelay().warn("Filled order " + orderId + " for " + trade);
		}		
	};
}
