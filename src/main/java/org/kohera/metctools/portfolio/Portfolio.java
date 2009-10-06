package org.kohera.metctools.portfolio;

import java.math.BigDecimal;
import java.util.Collection;

import org.marketcetera.trade.MSymbol;

public interface Portfolio {

	/**
	 * Add a trade to this portfolio.
	 * 
	 * Once added, the trade will automatically receive Trade and ExecutionReport
	 * events from the PortfolioStrategy, and will automatically update its state.
	 * @param trade
	 */
	public void addTrade( ITrade trade );
	
	/**
	 * Remove a trade from this portfolio by the Trade object.
	 * 
	 * @param trade
	 */
	public void removeTrade( ITrade trade );
	
	/**
	 * Remove a trade from this portfolio by symbol.
	 * 
	 * @param symbol
	 */
	public void removeTrade( MSymbol symbol );
	
	/**
	 * Returns true if and only if a Trade object for this symbol exists in the
	 * portfolio.
	 * 
	 * @param symbol
	 * @return
	 */
	public boolean hasTrade( MSymbol symbol );
	
	/**
	 * Returns the Trade object by symbol (or null).
	 * 
	 * @param symbol
	 * @return
	 */
	public ITrade getTrade( MSymbol symbol );
	
	/**
	 * Returns a Collection of all the trades in this Portfolio.
	 * 
	 * @return
	 */
	public Collection<ITrade> getTrades();
	
	public String[] getSymbols();
	
	/**
	 * Performs an Action for each trade in the portfolio.
	 * 
	 * @param action
	 */
	public void forEach( Action<ITrade> action );
	
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
	 * Set the fill policy for the entire portfolio.
	 * 
	 * @param policy
	 */
	public void setFillPolicy(FillPolicy policy);

	public void clearFillPolicy();
	

	
}
