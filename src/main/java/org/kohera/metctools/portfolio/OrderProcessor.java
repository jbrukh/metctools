package org.kohera.metctools.portfolio;

import java.math.BigDecimal;

import org.apache.log4j.Logger;
import org.marketcetera.trade.OrderSingle;

/**
 * 
 * Subclass of OrderProcessorBase that implements the order
 * interface.
 * 
 * @author Jake Brukhman
 *
 */
public class OrderProcessor extends OrderProcessorBase
	implements OrderInterface {

	/* logging */
	private final static Logger logger =
		Logger.getLogger(OrderProcessor.class);
	
	public OrderProcessor(Trade parent) {
		super(parent);
	}

	// BASE ORDERS //

	
	@Override
	public final void marketOrder( BigDecimal qty, Side side, long timeout, 
			OrderTimeoutPolicy policy, FillPolicy fillPolicy, boolean block ) {
		/* round to integer */
		qty = qty.setScale(0);
		OrderSingle order = getOrderBuilder()
		.makeMarket(parentTrade.getSymbol(), qty, side.toMetcSide())
		.getOrder();
		sendOrder(order, timeout, policy, fillPolicy, block);
	}
	
	@Override
	public final void marketOrder( BigDecimal qty, Side side, long timeout, 
			OrderTimeoutPolicy policy, boolean block ) {
		marketOrder(qty, side, timeout, policy, null, block);
	}

	@Override
	public final void marketOrder( BigDecimal qty, Side side, long timeout, 
			OrderTimeoutPolicy policy) {
		marketOrder(qty, side, timeout, policy, true);
	}
	
	@Override
	public final void marketOrder( BigDecimal qty, Side side, FillPolicy fillPolicy, boolean block ) {
		marketOrder(qty, side, parentTrade.getOrderTimeout(), 
				parentTrade.getOrderTimeoutPolicy(), fillPolicy, block);
	}

	@Override
	public final void marketOrder( BigDecimal qty, Side side, boolean block ) {
		marketOrder(qty, side, parentTrade.getOrderTimeout(), 
				parentTrade.getOrderTimeoutPolicy(), block);
	}


	// CANCELATION //

	@Override
	public final void cancel( final boolean block ) {
		cancelOrder(block);
	}

	@Override
	public final void cancel() {
		cancel(true);
	}
	
	@Override
	public final OrderInterface cancelAnd() {
		cancel();
		return this;
	}


	// CLOSE THE TRADE //

	@Override
	public final void closeMarket( long timeout, OrderTimeoutPolicy policy, 
			FillPolicy fillPolicy, boolean block ) {		

		/* if there is pending position, then cancel it */
		cancel(true);
		
		/* do nothing for non-open trades */
		if ( !parentTrade.isOpen()) {
			return;
		}

		marketOrder(parentTrade.getQty(), 
				parentTrade.getSide().opposite(), timeout, 
				policy, fillPolicy, block);
	}
	
	@Override
	public final void closeMarket( long timeout, OrderTimeoutPolicy policy, boolean block ) {
		closeMarket(timeout, policy, null, block);
	}

	@Override
	public final void closeMarket( long timeout, OrderTimeoutPolicy policy ) {
		closeMarket(timeout, policy, true);
	}

	@Override
	public final void closeMarket( FillPolicy fillPolicy, boolean block ) {
		closeMarket(parentTrade.getOrderTimeout(), 
				parentTrade.getOrderTimeoutPolicy(), fillPolicy, block );
	}
	
	@Override
	public final void closeMarket( boolean block ) {
		closeMarket(parentTrade.getOrderTimeout(), parentTrade.getOrderTimeoutPolicy(), block );
	}
	
	
	// TRADING //
	
	@Override
	public final void longMarket( BigDecimal qty, long timeout, 
			OrderTimeoutPolicy policy, FillPolicy fillPolicy,
			boolean block) {
		marketOrder(qty,Side.BUY,timeout,policy,fillPolicy, block);
	}
	
	@Override
	public final void longMarket( BigDecimal qty, long timeout, OrderTimeoutPolicy policy, boolean block) {
		longMarket(qty,timeout,policy,null, block);
	}
	
	@Override
	public final void longMarket( BigDecimal qty, long timeout, OrderTimeoutPolicy policy) {
		longMarket(qty,timeout,policy, true);	
	}

	@Override
	public final void longMarket( BigDecimal qty, FillPolicy fillPolicy, boolean block ) {
		longMarket(qty, parentTrade.getOrderTimeout(), 
				parentTrade.getOrderTimeoutPolicy(), fillPolicy, block);
	}

	@Override
	public final void longMarket( BigDecimal qty, boolean block ) {
		longMarket(qty, parentTrade.getOrderTimeout(), parentTrade.getOrderTimeoutPolicy(), block);
	}
	
	@Override
	public final void shortMarket( BigDecimal qty, long timeout, OrderTimeoutPolicy policy,
			FillPolicy fillPolicy, boolean block) {
		marketOrder(qty,Side.SELL,timeout,policy, fillPolicy, block);
	}
	
	@Override
	public final void shortMarket( BigDecimal qty, long timeout, OrderTimeoutPolicy policy, boolean block) {
		shortMarket(qty,timeout,policy, null, block);
	}
	
	@Override
	public final void shortMarket( BigDecimal qty, long timeout, OrderTimeoutPolicy policy) {
		longMarket(qty,timeout,policy, true);	
	}
	
	@Override
	public final void shortMarket( BigDecimal qty, FillPolicy fillPolicy, boolean block ) {
		longMarket(qty, parentTrade.getOrderTimeout(), 
				parentTrade.getOrderTimeoutPolicy(), block);
	}
	
	@Override
	public final void shortMarket( BigDecimal qty, boolean block ) {
		longMarket(qty, parentTrade.getOrderTimeout(), parentTrade.getOrderTimeoutPolicy(), block);
	}
	
	@Override
	public final void reduceMarket( BigDecimal qty, long timeout, 
			OrderTimeoutPolicy policy, FillPolicy fillPolicy, boolean block) {
		if ( qty.compareTo(parentTrade.getQty()) > 0) {  // should take care of the case with side == NONE
			logger.warn(">>> " + parentTrade + ": Cannot reduce more than you have.");
			return;
		}
		marketOrder(qty, parentTrade.getSide().opposite(), timeout, policy, fillPolicy, block );
	}
	
	@Override
	public final void reduceMarket( BigDecimal qty, long timeout, 
			OrderTimeoutPolicy policy, boolean block) {
		reduceMarket(qty, timeout, policy, null, block);
	}
	
	@Override
	public final void reduceMarket( BigDecimal qty, long timeout, OrderTimeoutPolicy policy ) {
		reduceMarket(qty, timeout, policy, true);
	}
	
	@Override
	public final void reduceMarket( BigDecimal qty, FillPolicy fillPolicy, boolean block ) {
		reduceMarket(qty, parentTrade.getOrderTimeout(), 
				parentTrade.getOrderTimeoutPolicy(), fillPolicy, block);
	}
	
	@Override
	public final void reduceMarket( BigDecimal qty, boolean block ) {
		reduceMarket(qty, parentTrade.getOrderTimeout(), parentTrade.getOrderTimeoutPolicy(), block);
	}

	@Override
	public final void augmentMarket( BigDecimal qty, long timeout, 
			OrderTimeoutPolicy policy, FillPolicy fillPolicy, boolean block) {
		marketOrder(qty, parentTrade.getSide(), timeout, policy, fillPolicy, block );
	}
	
	@Override
	public final void augmentMarket( BigDecimal qty, long timeout, OrderTimeoutPolicy policy, boolean block) {
		augmentMarket(qty, timeout, policy, null, block );
	}
	
	@Override
	public final void augmentMarket( BigDecimal qty, long timeout, OrderTimeoutPolicy policy) {
		augmentMarket(qty, timeout, policy, null, true );
	}
	
	@Override
	public final void augmentMarket( BigDecimal qty, FillPolicy fillPolicy, boolean block) {
		augmentMarket(qty, parentTrade.getOrderTimeout(), 
				parentTrade.getOrderTimeoutPolicy(), fillPolicy, block );
	}
	
	@Override
	public final void augmentMarket( BigDecimal qty, boolean block) {
		augmentMarket(qty, parentTrade.getOrderTimeout(), 
				parentTrade.getOrderTimeoutPolicy(), null, block );
	}
	
	
}
