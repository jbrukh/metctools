package org.kohera.metctools.portfolio;

import java.math.BigDecimal;

import org.kohera.metctools.util.OrderBuilder;
import org.kohera.metctools.util.Timer;
import org.kohera.metctools.util.Timer.Task;
import org.kohera.metctools.util.Timer.TaskThread;
import org.kohera.metctools.DelegatorStrategy;
import org.marketcetera.event.TradeEvent;
import org.marketcetera.trade.BrokerID;
import org.marketcetera.trade.ExecutionReport;
import org.marketcetera.trade.MSymbol;
import org.marketcetera.trade.OrderID;
import org.marketcetera.trade.OrderSingle;
import org.marketcetera.trade.OrderStatus;

public class Trade {

	/* internal fields  */
	transient private DelegatorStrategy parentStrategy;
	private final OrderProcessor orderProcessor;

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

	/* credentials */
	private final BrokerID brokerId;
	private final String account;

	/* policies */
	private long orderTimeout;
	public FillPolicy fillPolicy;
	public OrderTimeoutPolicy orderTimeoutPolicy;
	
	
	public Trade( DelegatorStrategy parent, MSymbol symbol, BrokerID brokerId, String account ) {
		this.symbol = symbol;
		parentStrategy = parent;
		quantity = leavesQuantity = pendingQuantity = BigDecimal.ZERO;
		costBasis = BigDecimal.ZERO;
		pendingOrderId = null;
		lastTrade = null;
		side = pendingSide = Side.NONE;
		
		this.brokerId = brokerId;
		this.account = account;
		
		fillPolicy = FillPolicies.ON_FILL_WARN;
		orderTimeoutPolicy = OrderTimeoutPolicies.ON_TIMEOUT_WARN;
		orderTimeout = 60*1000;
		
		orderProcessor = new OrderProcessor();
	}

	/* GETTERS */
	public BrokerID getBrokerId() {
		return brokerId;
	}

	public String getAccount() {
		return account;
	}
	
	public BigDecimal getCostBasis() {
		return costBasis;
	}

	
	public BigDecimal getLeavesQuantity() {
		return leavesQuantity;
	}

	
	public BigDecimal getPendingQuantity() {
		return pendingQuantity;
	}

	
	public BigDecimal getQuantity() {
		return quantity;
	}

	
	public MSymbol getSymbol() {
		return symbol;
	}

	
	public boolean isFilling() {
		return (pendingOrderId!=null && leavesQuantity.compareTo(BigDecimal.ZERO)!=0);
	}

	
	public boolean isPending() {
		return (pendingOrderId!=null);
	}
	
	
	public OrderID getPendingOrderID() {
		return pendingOrderId;
	}

	
	public Side getSide() {
		return side;
	}

	
	public BigDecimal getSignedQuantity() {
		return quantity.multiply(side.toBigDecimal());
	}

	
	public Side getPendingSide() {
		return pendingSide;
	}
	
	
	public BigDecimal getLastPrice() {
		if (lastTrade==null) return BigDecimal.ZERO;
		return lastTrade.getPrice();
	}

	
	public TradeEvent getLastTrade() {
		return lastTrade;
	}
	
	
	public BigDecimal getProfitLoss() {
		if (lastTrade==null) return BigDecimal.ZERO;
		return
		lastTrade.getPrice().subtract(costBasis).multiply(
				side.toBigDecimal()).multiply(quantity);
	}

	
	public BigDecimal getFillingQuantity() {
		BigDecimal polarity = BigDecimal.valueOf(side.value()*pendingSide.value(),0);
		BigDecimal alreadyFilled = pendingQuantity.subtract(leavesQuantity);
		return quantity.add( polarity.multiply(alreadyFilled) );
	}
	
	
	public DelegatorStrategy getParentStrategy() {
		return parentStrategy;
	}
	
	public void acceptTrade(TradeEvent tradeEvent) {
		lastTrade = tradeEvent;
	}
	
	public OrderProcessor order() {
		return orderProcessor;
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
	
	public final void acceptExecutionReport(DelegatorStrategy sender,
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
			orderProcessor.killTimeoutThread();
			
			/* execute the fill policy, if any */
			if ( fillPolicy != null ) {
				fillPolicy.onFill(parentStrategy, report.getOrderID(), this);
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

	
	public void setFillPolicy( FillPolicy policy ) {
		fillPolicy = policy;
	}
	
	
	public void setOrderTimeoutPolicy( OrderTimeoutPolicy policy ) {
		orderTimeoutPolicy = policy;
	}
	
	
	public void setOrderTimeout( long timeout ) {
		orderTimeout = timeout;
	}
	
	
	/**
	 * 
	 * Order Processor.
	 * 
	 */
	private final class OrderProcessor {
		
		private OrderBuilder orderBuilder;
		private final Timer timer;
		private TaskThread orderTimeoutThr;

		
		public OrderProcessor() {
			orderBuilder = new OrderBuilder(brokerId,account);
			timer = new Timer();
		}
		
		private void sendOrder( OrderSingle order, final long timeout, final OrderTimeoutPolicy policy ) {
					
			/* send the order */
			final OrderID id = order.getOrderID();
			pendingOrderId = parentStrategy.getRelay().sendOrder(order);
			parentStrategy.getRelay().info("Sending order " + id + ".");
			
			/* create timeout */
			orderTimeoutThr = timer.fireIn(timeout, new Task() {
				public void performTask() {
					if ( orderTimeoutPolicy!=null ) {
						orderTimeoutPolicy
						  .onOrderTimeout(parentStrategy, id, timeout, Trade.this);
					}
				}			
			});
		}
		
		public void killTimeoutThread() {
			if ( orderTimeoutThr!=null ) {
				timer.kill(orderTimeoutThr);
			}
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
}
