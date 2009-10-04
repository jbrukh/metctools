package org.kohera.metctools.portfolio;

import java.math.BigDecimal;

import org.kohera.metctools.AdvancedStrategy;
import org.marketcetera.event.TradeEvent;
import org.marketcetera.trade.ExecutionReport;
import org.marketcetera.trade.MSymbol;
import org.marketcetera.trade.OrderID;

public interface ITrade {

	/**
	 * Returns the symbol associated with this trade.
	 */
	public MSymbol getSymbol();
	
	/**
	 * Returns the current unsigned position of the trade (not including unfilled or filling orders).
	 * @return
	 */
	public BigDecimal getQuantity();
	
	/**
	 * Returns the current signed position of the trade (not including unfilled or filling orders).
	 * @return
	 */
	public BigDecimal getSignedQuantity();
	
	/**
	 * Returns the side of the trade.
	 * @return
	 */
	public Side getSide();
	
	/**
	 * Returns the quantity of shares that is currently being filled.
	 * @return
	 */
	public BigDecimal getPendingQuantity();
	
	/**
	 * Returns the portion of the pending position that has not yet been filled.
	 * @return
	 */
	public BigDecimal getLeavesQuantity();
	
	/**
	 * Returns the side of the order pending completion.
	 * @return
	 */
	public Side getPendingSide();
	
	/**
	 * Returns true if and only if there is an outstanding order for this trade.
	 * @return
	 */
	public boolean isPending();
	
	/**
	 * Returns true if and only if this trade is currently filling.
	 * @return
	 */
	public boolean isFilling();
	
	/**
	 * Returns the cost basis (entry price) for this trade.
	 * @return
	 */
	public BigDecimal getCostBasis();
	
	/**
	 * Return the OrderID of the pending order (or null).
	 * @return
	 */
	public OrderID getPendingOrderID();
	
	/**
	 * Returns original position together with the portion of the pending
	 * position that has already been filled.  If the trade is not filling,
	 * then it returns the same as getQuantity().
	 * 
	 * If the fills up to this point have changed the side of the original trade,
	 * the quantity returned will have a negative sign.
	 * 
	 * @return
	 */
	public BigDecimal getFillingQuantity();
	
	/**
	 * Returns the last trade event for this Trade.
	 * @return
	 */
	public TradeEvent getLastTrade();
	
	/**
	 * Returns the last trade price.
	 * @return
	 */
	public BigDecimal getLastPrice();

	/**
	 * Returns the profit-loss relative to the last trade price.
	 * @return
	 */
	public BigDecimal getProfitLoss();

	/**
	 * Updates the state of the trade based on a TradeEvent.
	 * 
	 * @param tradeEvent
	 */
	public void acceptTrade(TradeEvent tradeEvent);

	/**
	 * Updates the state of the trade based on a new ExecutionReport.
	 * 
	 * @param sender
	 * @param report
	 */
	public void acceptExecutionReport(AdvancedStrategy sender, ExecutionReport report);

	public AdvancedStrategy getParentStrategy();

	public void setOrderTimeoutPolicy(OrderTimeoutPolicy policy);

	public void setOrderTimeout(long timeout);

	public void marketOrder(BigDecimal qty, Side side, long timeout,
			OrderTimeoutPolicy policy);

	public void marketOrder(BigDecimal qty, Side side, long timeout);

	void marketOrder(BigDecimal qty, Side side);

	void longTradeMarket(BigDecimal qty, long timeout, OrderTimeoutPolicy policy);

	void longTradeMarket(BigDecimal qty, long timeout);

	void longTradeMarket(BigDecimal qty);

	void shortTradeMarket(BigDecimal qty, long timeout,
			OrderTimeoutPolicy policy);

	void shortTradeMarket(BigDecimal qty, long timeout);

	void shortTradeMarket(BigDecimal qty);

	void closeTradeMarket(long timeout, OrderTimeoutPolicy policy);

	void closeTradeMarket(long timeout);

	void closeTradeMarket();

	void reduceTradeMarket(BigDecimal qty, long timeout,
			OrderTimeoutPolicy policy);

	void reduceTradeMarket(BigDecimal qty, long timeout);

	void reduceTradeMarket(BigDecimal qty);
	
	
	
}
