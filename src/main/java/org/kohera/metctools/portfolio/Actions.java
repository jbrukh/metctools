package org.kohera.metctools.portfolio;

/**
 * Collection of common Actions (to be performed on Trades
 * in a portfolio.)
 * 
 * @author Jake Brukhman
 *
 */
public class Actions {

	// COMMON ACTIONS //
	
	public final static Action<Trade> CANCEL_ALL = new Action<Trade>() {
		@Override
		public void performAction(Trade trade) {
			trade.order().cancel();
		}
	};

	public final static Action<Trade> CLOSE_ALL = new Action<Trade>() {
		@Override
		public void performAction(Trade trade) {
			trade.order().closeMarket(false);
		}
	};
	
}
