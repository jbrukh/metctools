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
	
	public final static Action CANCEL_ORDER = new Action() {
		@Override
		public void performAction(Trade trade) {
			if ( trade.isPending() ) {
				trade.order().cancel();
			}
		}
	};
	
	public final static Action CANCEL_ORDER_ASYNC = new Action() {
		@Override
		public void performAction(Trade trade) {
			if ( trade.isPending() ) {
				trade.order().cancel(false);
			}
		}
	};

	public final static Action CLOSE_POSITION = new Action() {
		@Override
		public void performAction(Trade trade) {
			trade.order().closeMarket(true);
		}
	};
	
	public final static Action CLOSE_POSITION_ASYNC = new Action() {
		@Override
		public void performAction(Trade trade) {
			trade.order().closeMarket(false);
		}
	};
	
}
