package org.kohera.metctools.portfolio;

import java.math.BigDecimal;

import org.kohera.metctools.util.OrderBuilder;
import org.kohera.metctools.util.Timer;
import org.kohera.metctools.util.Timer.Task;
import org.kohera.metctools.util.Timer.TaskThread;
import org.kohera.metctools.AdvancedStrategy;
import org.marketcetera.event.TradeEvent;
import org.marketcetera.trade.BrokerID;
import org.marketcetera.trade.ExecutionReport;
import org.marketcetera.trade.MSymbol;
import org.marketcetera.trade.OrderID;
import org.marketcetera.trade.OrderSingle;
import org.marketcetera.trade.OrderStatus;

public class TradeBase implements Trade {

	/* internal fields  */
	transient private AdvancedStrategy parentStrategy;

	/* fields for accounting */
	private final MSymbol symbol;
	private BigDecimal quantity;
	private BigDecimal leavesQuantity;
	private BigDecimal pendingQuantity;
	private Side side;
	private Side pendingSide;
	private OrderID pendingOrderId;
	private BigDecimal costBasis;
	private TradeEvent lastTrade;

	/* fields for trading */
	private final OrderBuilder orderBuilder;
	private final Timer timer;
	private long orderTimeout;
	private TaskThread orderTimeoutThr;
	public OrderTimeoutPolicy orderTimeoutPolicy;
	
	
	public TradeBase( AdvancedStrategy parent, MSymbol symbol, BrokerID brokerId, String account ) {
		this.symbol = symbol;
		parentStrategy = parent;
		quantity = leavesQuantity = pendingQuantity = BigDecimal.ZERO;
		costBasis = BigDecimal.ZERO;
		pendingOrderId = null;
		lastTrade = null;
		side = pendingSide = Side.NONE;
		orderBuilder = new OrderBuilder(brokerId,account);
		timer = new Timer();
		
		orderTimeoutPolicy = OrderTimeoutPolicies.ON_TIMEOUT_WARN;
		orderTimeout = 60*1000;
	}

	/* GETTERS */
	
	@Override
	public BigDecimal getCostBasis() {
		return costBasis;
	}

	@Override
	public BigDecimal getLeavesQuantity() {
		return leavesQuantity;
	}

	@Override
	public BigDecimal getPendingQuantity() {
		return pendingQuantity;
	}

	@Override
	public BigDecimal getQuantity() {
		return quantity;
	}

	@Override
	public MSymbol getSymbol() {
		return symbol;
	}

	@Override
	public boolean isFilling() {
		return (pendingOrderId!=null && leavesQuantity.compareTo(BigDecimal.ZERO)!=0);
	}

	@Override
	public boolean isPending() {
		return (pendingOrderId!=null);
	}
	
	@Override
	public OrderID getPendingOrderID() {
		return pendingOrderId;
	}

	@Override
	public Side getSide() {
		return side;
	}

	@Override
	public BigDecimal getSignedQuantity() {
		return quantity.multiply(side.toBigDecimal());
	}

	@Override
	public Side getPendingSide() {
		return pendingSide;
	}
	
	@Override
	public BigDecimal getLastPrice() {
		if (lastTrade==null) return BigDecimal.ZERO;
		return lastTrade.getPrice();
	}

	@Override
	public TradeEvent getLastTrade() {
		return lastTrade;
	}
	
	@Override
	public BigDecimal getProfitLoss() {
		if (lastTrade==null) return BigDecimal.ZERO;
		return
		lastTrade.getPrice().subtract(costBasis).multiply(
				side.toBigDecimal()).multiply(quantity);
	}

	@Override
	public BigDecimal getFillingQuantity() {
		BigDecimal polarity = BigDecimal.valueOf(side.value()*pendingSide.value(),0);
		BigDecimal alreadyFilled = pendingQuantity.subtract(leavesQuantity);
		return quantity.add( polarity.multiply(alreadyFilled) );
	}
	
	@Override
	public AdvancedStrategy getParentStrategy() {
		return parentStrategy;
	}
	
	@Override
	public void acceptTrade(TradeEvent tradeEvent) {
		lastTrade = tradeEvent;
	}

	/**
	 * Adjusts the trade information based on an incoming execution report.
	 * 
	 * TODO: WARNING: In the current implementation, ProfitLoss may not be correct if
	 * the trade switches sides.
	 * 
	 * @param sender
	 * @param report
	 */
	@Override
	public final void acceptExecutionReport(AdvancedStrategy sender,
			ExecutionReport report) {
		/* check the correct symbol */
		if ( symbol!=report.getSymbol()) {
			sender.getRelay().warn(
					symbol + ": received external execution report (ignoring).");
			return;
		}
		
		/* check the correct order id */
		if ( !isPending() || pendingOrderId!=report.getOrderID()) {
			sender.getRelay().warn(
					symbol + ": received external execution report (accepting).");
		}
		
		pendingSide = Side.fromMetcSide(report.getSide());
		pendingQuantity = report.getCumulativeQuantity();
		leavesQuantity = report.getLeavesQuantity();

		/* assign a side if you haven't already */
		if (side==Side.NONE) {
			side = pendingSide;
		}
		
			
		/* if this is the last report... */
		if ( report.getOrderStatus() == OrderStatus.Filled ) {
			
			/* check the polarity */
			BigDecimal polarity = BigDecimal.valueOf(side.value()*pendingSide.value(),0); // 1 if same, -1 if opposite

			/* switch sides if necessary */
			if ( polarity.compareTo(BigDecimal.ZERO)<0 && pendingQuantity.compareTo(quantity)>0 ) {
				side = side.opposite();
			}
			
			/* update the position */
			quantity = quantity.add(polarity.multiply(pendingQuantity));
			
			/* update average price */
			if ( polarity.compareTo(BigDecimal.ZERO)>0 ) {
				/* calculate average price */
				BigDecimal totalQty = quantity.add(pendingQuantity);
				BigDecimal avg = quantity.multiply(costBasis).add(pendingQuantity.multiply(report.getAveragePrice()));
				costBasis = avg.divide(totalQty, BigDecimal.ROUND_HALF_EVEN);
			}
			
			pendingQuantity = BigDecimal.ZERO;
			leavesQuantity = BigDecimal.ZERO;  // TODO: check in fact this is zero.		
		
			/* kill the timeout thread, if any */
			if ( orderTimeoutThr!=null ) {
				timer.kill(orderTimeoutThr);
			}
		}
		
	}
	
	public String toString() {
		return String.format("{%s:[%.2f]:%s%d@%.4f}",
				getSymbol(),
				getLastPrice().floatValue(),
				(side==Side.BUY?"+":(side==Side.SELL?"-":"")),
				quantity,
				costBasis.floatValue());
	}

	
	
	/**
	 * TRADING FUNCTIONALITY
	 */

	@Override
	public void setOrderTimeoutPolicy( OrderTimeoutPolicy policy ) {
		orderTimeoutPolicy = policy;
	}
	
	@Override
	public void setOrderTimeout( long timeout ) {
		orderTimeout = timeout;
	}
	
	
	private void sendOrder( OrderSingle order, final long timeout, final OrderTimeoutPolicy policy ) {
				
		/* send the order */
		final OrderID id = order.getOrderID();
		pendingOrderId = parentStrategy.getRelay().sendOrder(order);
		parentStrategy.getRelay().info("Sending order " + id + ".");
		
		/* create timeout */
		orderTimeoutThr = timer.fireIn(timeout, new Task() {
			public void performTask() {
				orderTimeoutPolicy.onOrderTimeout(parentStrategy, id, timeout);
			}			
		});
	}
	
	public void marketOrder( BigDecimal qty, Side side, long timeout, OrderTimeoutPolicy policy ) {
		OrderSingle order = orderBuilder
								.makeMarket(symbol, qty, side.toMetcSide())
								.getOrder();
		sendOrder(order,timeout,policy);
	}
	
	public void marketOrder( BigDecimal qty, Side side, long timeout ) {
		marketOrder(qty,side,timeout,orderTimeoutPolicy);
	}

	public void marketOrder( BigDecimal qty, Side side ) {
		marketOrder(qty,side,orderTimeout,orderTimeoutPolicy);
	}
	
	public void longTradeMarket(BigDecimal qty, long timeout, OrderTimeoutPolicy policy) {
		marketOrder(qty,Side.BUY,timeout,policy);
	}
	
	public void longTradeMarket(BigDecimal qty, long timeout) {
		longTradeMarket(qty, timeout, orderTimeoutPolicy);
	}

	public void longTradeMarket(BigDecimal qty) {
		longTradeMarket(qty, orderTimeout, orderTimeoutPolicy);
	}

	public void shortTradeMarket(BigDecimal qty, long timeout, OrderTimeoutPolicy policy) {
		marketOrder(qty,Side.SELL,timeout,policy);
	}
	
	public void shortTradeMarket(BigDecimal qty, long timeout) {
		shortTradeMarket(qty, timeout, orderTimeoutPolicy);
	}

	public void shortTradeMarket(BigDecimal qty) {
		shortTradeMarket(qty, orderTimeout, orderTimeoutPolicy);
	}
	
	public void closeTradeMarket(long timeout, OrderTimeoutPolicy policy) {
		reduceTradeMarket(quantity,timeout,policy);
	}
	
	public void closeTradeMarket(long timeout) {
		closeTradeMarket(timeout,orderTimeoutPolicy);
	}
	
	public void closeTradeMarket() {
		closeTradeMarket(orderTimeout,orderTimeoutPolicy);
	}

	public void reduceTradeMarket(BigDecimal qty, long timeout, OrderTimeoutPolicy policy) {
		marketOrder(qty,side.opposite(),timeout,policy);
	}

	public void reduceTradeMarket(BigDecimal qty, long timeout) {
		reduceTradeMarket(qty,timeout,orderTimeoutPolicy);
	}
	
	public void reduceTradeMarket(BigDecimal qty) {
		reduceTradeMarket(qty,orderTimeout,orderTimeoutPolicy);
	}
}
