package org.kohera.metctools.portfolio;

import org.apache.log4j.Logger;
import org.kohera.metctools.DelegatorStrategy;
import org.kohera.metctools.Messages;
import org.marketcetera.trade.OrderID;


/**
 * 
 * Various pre-programmed FillPolicies.
 * 
 * @author Jake Brukhman
 *
 */
public class FillPolicies {

	/**
	 * Sends a message to the log when a fill has been registered.
	 */
	public final static FillPolicy ON_FILL_WARN = new FillPolicy() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 7091833875830783138L;

		@Override
		public void onFill(DelegatorStrategy sender, OrderID orderId,
				Trade trade) {

			/* default message */
			Logger.getLogger(PortfolioStrategy.class).info(
					Messages.MSG_ON_FILL_INFO(trade, orderId)
			);
		}		
	};
}
