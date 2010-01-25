package org.kohera.metctools.portfolio;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;

import org.marketcetera.trade.BrokerID;

public interface Portfolio extends Serializable {

	/**
	 * Add a trade to this portfolio.
	 * 
	 * Once added, the trade will automatically receive Trade and ExecutionReport
	 * events from the PortfolioStrategy, and will automatically update its state.
	 * @param trade
	 */
	public void addTrade( Trade trade );
	
	/**
	 * Remove a trade from this portfolio by the Trade object.
	 * 
	 * Note that you must liquidate and zero the position before removal is
	 * allowed.
	 * 
	 * @throws RuntimeException
	 * @param trade
	 */
	public void removeTrade( Trade trade );
	
	/**
	 * Remove a trade from this portfolio by symbol.  Finds the
	 * associated Trade object and attempts to remove it.
	 * 
	 * Note that you must liquidate and zero the position before removal is
	 * allowed.
	 * 
	 * @throws RuntimeException
	 * @param symbol
	 */
	public void removeTrade( String symbol );
	
	/**
	 * Returns true if and only if a Trade object for this symbol exists in the
	 * portfolio.
	 * 
	 * @param symbol
	 * @return
	 */
	public boolean hasTrade( String symbol );
	
	/**
	 * Returns the Trade object by symbol (or null).
	 * 
	 * @param symbol
	 * @return
	 */
	public Trade getTrade( String symbol );
	
	/**
	 * Returns a Collection of all the trades in this Portfolio.
	 * 
	 * @return
	 */
	public Collection<Trade> getTrades();
	
	/**
	 * Returns an array of symbols that represent the trades
	 * in the Portfolio.
	 * 
	 * @return
	 */
	public Collection<String> getSymbols();
	
	/**
	 * Performs an Action for each trade in the portfolio.
	 * 
	 * @param action
	 */
	public void forEach( Action<Trade> action );
	
	/**
	 * Set the order timeout policy for the entire portfolio.
	 * 
	 * Once this method is called, all existing and future trades
	 * will adhere to the specified OrderTimeoutPolicy, not counting
	 * orders that are already in progress.
	 * 
	 * @param policy
	 */
	public void setOrderTimeoutPolicy( OrderTimeoutPolicy policy );
	
	/**
	 * Set the order timeout (in milliseconds) for the entire portfolio.
	 * 
	 * Once this method is called, all existing and future trades will
	 * adhere to the specified timeout time, not counting orders that are
	 * already in progress.
	 * 
	 * @param timeout
	 */
	public void setOrderTimeout( long timeout );
	
	/**
	 * Clear the order timeout policy for the entire portfolio.
	 * 
	 * Once this method is called, future trades added to the portfolio
	 * will be responsible for setting their own order timeout policy.  If
	 * setOrderTimeoutPolicy() had been called on a non-empty portfolio,
	 * those trades will continue to adhere to that policy until manually
	 * changed on an individual basis.
	 * 
	 */
	public void clearOrderTimeoutPolicy();
	
	/**
	 * Clear the order timeout for the entire portfolio.
	 * 
	 * (See clearOrderTimeoutPolicy().)
	 */
	public void clearOrderTimeout();
	
	/**
	 * Returns the sum of all signed positions in the portfolio, on
	 * a best effort basis.  If some positions do not have a profit-loss,
	 * they are ignored.
	 * 
	 * @return
	 */
	public BigDecimal getTotalPosition();

	/**
	 * Set the fill policy for the entire Portfolio.
	 * 
	 * @param policy
	 */
	public void setFillPolicy(FillPolicy policy);

	/**
	 * Clear the fill policy for the entire Portfolio.
	 */
	public void clearFillPolicy();
	
	/**
	 * Set the default order reject policy.
	 * 
	 * @param policy
	 */
	public void setRejectPolicy(RejectPolicy policy);
	
	/**
	 * Clear the default order reject policy.
	 */
	public void clearRejectPolicy();
	
	/**
	 * Get the number of Trades in the Portfolio.
	 * 
	 * @return
	 */
    public int size();

    /**
     * Set the account information for the portfolio.  (This is
     * necessary to trade.)
     * 
     * @param brokerId
     * @param account
     */
	public void setAccountInfo(BrokerID brokerId, String account);

	/**
	 * Returns the broker id.
	 * 
	 * @return
	 */
	public BrokerID getBrokerID();

	/**
	 * Returns the account.
	 * 
	 * @return
	 */
	public String getAccount();

	/**
	 * Returns the parent PortfolioStrategy to whom this portfolio belongs.
	 * 
	 * @return
	 */
	public PortfolioStrategy getParentStrategy();
	
	/**
	 * Sets the parent PortfolioStrategy to whom this portfolio belongs.
	 * 
	 * @param parent
	 */
	public void setParentStrategy(PortfolioStrategy parent);
	
	/**
	 * 
	 * @param symbol
	 * @return
	 */
	public Trade createTrade(String symbol);

	/**
	 * Removes a trade from portfolio, no questions asked.
	 * 
	 * @param trade
	 */
	public void forcefullyRemoveTrade(Trade trade);

	/**
	 * Returns true if and only if the account information is set for this
	 * portfolio.
	 * 
	 * @return
	 */
	public boolean isAccountInfoSet();
	

}
