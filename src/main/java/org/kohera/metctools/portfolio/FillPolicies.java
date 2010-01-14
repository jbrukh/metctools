package org.kohera.metctools.portfolio;

import org.kohera.metctools.DelegatorStrategy;
import org.marketcetera.trade.OrderID;

/**
 * Various pre-programmed FillPolicies.
 * 
 * @author Jake Brukhman
 *
 */
public class FillPolicies {

	/**
	 * Sends a WARN LogEvent to the parent strategy to let it know that
	 * an order has been filled.
	 */
	public final static FillPolicy ON_FILL_WARN = new FillPolicy() {
		@Override
		public void onFill(DelegatorStrategy sender, OrderID orderId,
				Trade trade) {
			sender.getFramework().warn("Filled order " + orderId + " for " + trade);
		}		
	};
}
