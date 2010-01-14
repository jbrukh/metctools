package org.kohera.metctools.portfolio;

import org.apache.log4j.Logger;
import org.kohera.metctools.DelegatorStrategy;
import org.marketcetera.trade.ExecutionReport;
import org.marketcetera.trade.OrderID;

public class RejectPolicies {
	
	/**
	 * Sends a WARN LogEvent to the parent strategy to let it know that
	 * an order has been filled.
	 */
	public final static RejectPolicy ON_REJECT_WARN = new RejectPolicy() {
		@Override
		public void onReject(DelegatorStrategy sender, OrderID orderId,
				Trade trade, ExecutionReport report) {
			Logger.getLogger(PortfolioStrategy.class)
			  .info(">>> " + trade + ": REJECTED (" + report.getText() + ") Order " + orderId + ".");
		}		
	};

}
