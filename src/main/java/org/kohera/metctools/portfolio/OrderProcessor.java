package org.kohera.metctools.portfolio;

import java.math.BigDecimal;

import org.marketcetera.trade.OrderSingle;

public class OrderProcessor extends OrderProcessorBase {

	public OrderProcessor(Trade parent) {
		super(parent);
	}


	/**
	 * Send a market order.
	 * 
	 * @param qty
	 * @param side
	 * @param timeout
	 * @param policy
	 */
	public final void marketOrder( BigDecimal qty, Side side, long timeout, 
			OrderTimeoutPolicy policy, boolean block ) {
		/* round to integer */
		qty = qty.setScale(0);
		OrderSingle order = getOrderBuilder()
								.makeMarket(parentTrade.getSymbol(), qty, side.toMetcSide())
								.getOrder();
		sendOrder(order, timeout, policy, block);
	}
	
	public final void marketOrder( BigDecimal qty, Side side, 
			OrderTimeoutPolicy policy, long timeout) {
		marketOrder(qty, side, timeout, policy, true);
	}

	public final void longMarket( BigDecimal qty, long timeout, OrderTimeoutPolicy policy) {
		longMarket(qty,timeout,policy, true);	
	}
	
	public final void longMarket( BigDecimal qty, long timeout, OrderTimeoutPolicy policy, boolean block) {
		marketOrder(qty,Side.BUY,timeout,policy, block);
	}
	
	public final void shortMarket( BigDecimal qty, long timeout, OrderTimeoutPolicy policy) {
		shortMarket(qty, timeout, policy, true);
	}
	public final void shortMarket( BigDecimal qty, long timeout, OrderTimeoutPolicy policy, boolean block) {
		marketOrder(qty, Side.SELL, timeout, policy, block);
	}
	
	public final void closeMarket( long timeout, OrderTimeoutPolicy policy, boolean block ) {		
		
		/* if there is pending position, then cancel it */
		if ( isPending() ) {
			cancelOrder(true);
		}
		
		/* do nothing for non-open trades */
		if ( !parentTrade.isOpen()) {
			return;
		}
		
	
		marketOrder(parentTrade.getQuantity(), 
				parentTrade.getSide().opposite(), timeout, policy, block);
	}
	
	public final void closeMarket( long timeout, OrderTimeoutPolicy policy ) {
		closeMarket(timeout, policy, true);
	}
	
	public final void cancel( final boolean block ) {
		if ( isPending() ) {
			cancelOrder(block);
		}
	}
}
