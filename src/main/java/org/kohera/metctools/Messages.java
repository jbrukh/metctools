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
	public static final String MSG_DATA_PROVIDER_NOT_SET =
		">>> You must set the data provider!";
	
	public static final String MSG_MARKET_DATA_FAILURE =
		">>> Not able to start the market data!";
	
	
	/* default policy messages */
	public static final String MSG_ON_FILL_INFO(Trade trade, OrderID orderId) {
		return ">>> " + trade + ": Filled order " + orderId + ".";
	}
	
	public static final String MSG_ON_TIMEOUT_WARN(long timeout, Trade trade) {
		return ">>> " + trade + ": Could not fill order in " + timeout + " ms. (Ignoring.)";
	}
	
	public static final String MSG_ON_TIMEOUT_CANCEL(long timeout) {
		return ">>> Could not fill order in " + timeout + " ms. (Canceling.)";
	}
	
	// TRADE MESSAGES //
	public static final String MSG_EXTERNAL_REPORT(Trade trade) {
		return ">>> " + trade + ": received external execution report (ignoring).";
	}
}
