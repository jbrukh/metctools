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
	transient private 
		DelegatorStrategy 	parentStrategy;		// parent strategy
	private final 
		OrderProcessor 		orderProcessor;		// order processing object

	/* accounting */
	private final MSymbol 	symbol;				// underlying symbol
	private BigDecimal 		quantity;			// unsigned position
	private BigDecimal 		leavesQuantity;		// leavesQuantity of the pending order
	private BigDecimal 		pendingQuantity;	// number of shares pending fill
	private Side 			side;				// side of the position
	private Side 			pendingSide;		// side of the incoming fills
	private OrderID 		pendingOrderId;		// orderId of the order being filled
	private BigDecimal 		costBasis;			// average entry price of position
	private TradeEvent 		lastTrade;			// last trade of the underylying symbol

	/* credentials */
	private final BrokerID 	brokerId;			// broker associated with this trade
	private final String 	account;			// account associated with this trade

	/* policies */
	private long 			orderTimeout;		// default timeout in milliseconds
	public FillPolicy 		fillPolicy;			// default fill policy (what to do on a fill?)
	public OrderTimeoutPolicy orderTimeoutPolicy; // default order timeout policy (what to do if order times out?)
	
	
	/**
	 * Create a new Trade object instance.
	 * 
	 * @param parent
	 * @param symbol
	 * @param brokerId
	 * @param account
	 */
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

	/**
	 * Returns the brokerId.
	 * 
	 * @return
	 */
	public BrokerID getBrokerId() {
		return brokerId;
	}

	/**
	 * Returns the account.
	 * 
	 * @return
	 */
	public String getAccount() {
		return account;
	}
	
	/**
	 * Returns the cost basis (average price of position).
	 * 
	 * @return
	 */
	public BigDecimal getCostBasis() {
		return costBasis;
	}

	/**
	 * Returns the number of shares still pending fill.
	 * 
	 * @return
	 */
	public BigDecimal getLeavesQuantity() {
		return leavesQuantity;
	}

	/**
	 * Returns the total number of shares comprise a pending order.
	 * 
	 * @return
	 */
	public BigDecimal getPendingQuantity() {
		return pendingQuantity;
	}

	/**
	 * Returns the unsigned number of shares of this position.
	 * 
	 * This does not take into consideration a currently filling order,
	 * even if some of the order has been filled.  (See getPendingQuantity()
	 * and getLeavesQuantity().)
	 * 
	 * @return
	 */
	public BigDecimal getQuantity() {
		return quantity;
	}

	/**
	 * Returns the underlying symbol of this Trade.
	 * 
	 * @return
	 */
	public MSymbol getSymbol() {
		return symbol;
	}

	/**
	 * Returns true if the Trade is currently has a pending order
	 * that is filling.
	 * 
	 * @return
	 */
	public boolean isFilling() {
		return (pendingOrderId!=null && leavesQuantity.compareTo(BigDecimal.ZERO)!=0);
	}

	/**
	 * Returns true if the Trade currently has a pending order.
	 * 
	 * @return
	 */
	public boolean isPending() {
		return (pendingOrderId!=null);
	}
	
	/**
	 * Returns the orderId of the pending order (or null).
	 * 
	 * @return
	 */
	public OrderID getPendingOrderID() {
		return pendingOrderId;
	}

	/**
	 * Returns the side of the trade (or Side.NONE).
	 * 
	 * @return
	 */
	public Side getSide() {
		return side;
	}

	/**
	 * Returns the signed position of the Trade.
	 * 
	 * @return
	 */
	public BigDecimal getSignedQuantity() {
		return quantity.multiply(side.toBigDecimal());
	}

	/**
	 * Returns the side of the pending order.
	 * 
	 * @return
	 */
	public Side getPendingSide() {
		return pendingSide;
	}

	/**
	 * Returns the last trading price of the underlying symbol.
	 * 
	 * In order for this method to return an accurate value,
	 * data flows for this symbol must have been turned on,
	 * and this Trade must reside in a PortfolioStrategy's
	 * Portfolio.
	 * 
	 * @return
	 */
	public BigDecimal getLastPrice() {
		if (lastTrade==null) return BigDecimal.ZERO;
		return lastTrade.getPrice();
	}

	/**
	 * Returns the last TradeEvent.
	 * 
	 * In order for this method to return an accurate value,
	 * data flows for this symbol must have been turned on,
	 * and this Trade must reside in a PortfolioStrategy's
	 * Portfolio.
	 * 
	 * @return
	 */
	public TradeEvent getLastTrade() {
		return lastTrade;
	}
	
	/**
	 * Returns the number of shares that have been filled up to this
	 * point.
	 * 
	 * @return
	 */
	public BigDecimal getFillingQuantity() {
		BigDecimal polarity = BigDecimal.valueOf(side.value()*pendingSide.value(),0);
		BigDecimal alreadyFilled = pendingQuantity.subtract(leavesQuantity);
		return quantity.add( polarity.multiply(alreadyFilled) );
	}
	
	/**
	 * Returns a reference to the parent strategy for this Trade.
	 * 
	 * @return
	 */
	public DelegatorStrategy getParentStrategy() {
		return parentStrategy;
	}
	
	/**
	 * Interface for getting the latest TradeEvent.
	 * 
	 * @param tradeEvent
	 */
	public void acceptTrade(TradeEvent tradeEvent) {
		lastTrade = tradeEvent;
	}

	/**
	 * Returns the OrderProcessor object for this Trade.
	 * 
	 * @return
	 */
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
	
	/**
	 * Formats this Trade object for text output.
	 */
	public String toString() {
		return String.format("{%s:[%.2f]:%s%d@%.4f}",
				getSymbol(),
				getLastPrice().floatValue(),
				(side==Side.BUY?"+":(side==Side.SELL?"-":"")),
				quantity.intValue(),
				costBasis.floatValue());
	}

	/**
	 * Sets the FillPolicy for this Trade.
	 * 
	 * When an order is successfully filled, the FillPolicy.onFill()
	 * method is executed.
	 * 
	 * @param policy
	 */
	public void setFillPolicy( FillPolicy policy ) {
		fillPolicy = policy;
	}
	
	/**
	 * Sets the OrderTimeoutPolicy for this Trade.
	 * 
	 * When an order has not been completely filled by the time
	 * an order timeout occurs, then this policy is executed.
	 * 
	 * @param policy
	 */
	public void setOrderTimeoutPolicy( OrderTimeoutPolicy policy ) {
		orderTimeoutPolicy = policy;
	}
	
	/**
	 * Sets the order timeout time (in ms.) for this Trade.
	 * 
	 * @param timeout
	 */
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
