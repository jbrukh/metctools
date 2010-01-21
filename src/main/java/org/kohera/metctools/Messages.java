package org.kohera.metctools;

import org.kohera.metctools.portfolio.Trade;
import org.marketcetera.trade.OrderID;

/**
 * 
 * Error messages for Strategy wrappers.
 * 
 * @author Jake Brukhman
 *
 */
public class Messages {

	/* some errors */
	
	public static final String DATA_PROVIDER_NOT_SET =
		"You must set the data provider.";
	
	public static final String MARKET_DATA_FAILURE =
		"Was not able to start the market data.";
	
	
	/* default policy messages */
	public static final String MSG_ON_FILL_INFO(Trade trade, OrderID orderId) {
		return ">>> " + trade + ": Filled order " + orderId + ".";
	}
	
	public static final String MSG_ON_TIMEOUT_WARN(long timeout) {
		return ">>> Could not fill order in " + timeout + " ms. (Ignoring.)";
	}
	
	public static final String MSG_ON_TIMEOUT_CANCEL(long timeout) {
		return ">>> Could not fill order in " + timeout + " ms. (Canceling.)";
	}
}
