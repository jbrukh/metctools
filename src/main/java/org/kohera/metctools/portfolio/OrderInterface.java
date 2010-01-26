package org.kohera.metctools.portfolio;

import java.math.BigDecimal;

/**
 * The order interface.
 * 
 * @author Jake Brukhman
 *
 */
public interface OrderInterface {
	
	// BASE ORDERS //
	
	/**
	 * Place a market order.
	 * 
	 * @param qty 	number of shares 
	 * @param side	side of the order
	 * @param timeout	timeout milliseconds (0 = never)
	 * @param policy		
	 * @param block	whether to block until order completes
	 */
	void marketOrder( BigDecimal qty, Side side, long timeout, 
						OrderTimeoutPolicy policy, boolean block );

	/**
	 * Place a market order and block until the transaction completes.
	 * 
	 * @param qty
	 * @param side
	 * @param timeout
	 * @param policy
	 */
	void marketOrder( BigDecimal qty, Side side, long timeout, 
			OrderTimeoutPolicy policy );
	
	/**
	 * Place a market order using the Trade's default timeout time and
	 * OrderTimeoutPolicy.
	 * 
	 * @param qty
	 * @param side
	 * @param block
	 */
	void marketOrder( BigDecimal qty, Side side, boolean block);
	
	
	// CANCELATION //
	
	/**
	 * Cancel the current pending order, if there is one.
	 * 
	 */
	void cancel( boolean block );
	
	/**
	 * Cancel the current pending order, and block until the cancelation
	 * is either completed or rejected.
	 * 
	 */
	void cancel();
	
	/**
	 * Cancel the current pending order, and perform another action in
	 * through the order interface.
	 * 
	 * @return
	 */
	OrderInterface cancelAnd();
	
	
	// CLOSING //
	
	/**
	 * Close the position using a market order.
	 */
	void closeMarket( long timeout, OrderTimeoutPolicy policy, boolean block );
	
	/**
	 * Close the position using a market order, and block until the
	 * transaction completes.
	 * 
	 * @param timeout
	 * @param policy
	 */
	void closeMarket( long timeout, OrderTimeoutPolicy policy );
	
	/**
	 * Close the position using a market order, and the trade's default
	 * timeout time and OrderTimeoutPolicy.
	 * 
	 * @param block
	 */
	void closeMarket( boolean block );
	
	
	
	
	// TRADING //
	
	/**
	 * Go long.
	 * 
	 * @param qty
	 * @param timeout
	 * @param policy
	 * @param block
	 */
	void longMarket( BigDecimal qty, long timeout, OrderTimeoutPolicy policy, boolean block );
	
	/**
	 * Go long and block until the transaction completes.
	 * 
	 * @param qty
	 * @param timeout
	 * @param policy
	 */
	void longMarket( BigDecimal qty, long timeout, OrderTimeoutPolicy policy);
	
	/**
	 * Go long using the Trade's default timeout time and OrderTimeoutPolicy.
	 * 
	 * @param qty
	 * @param block
	 */
	void longMarket( BigDecimal qty, boolean block );
	
	/**
	 * Go short.
	 * 
	 * @param qty
	 * @param timeout
	 * @param policy
	 * @param block
	 */
	void shortMarket( BigDecimal qty, long timeout, OrderTimeoutPolicy policy, boolean block );
	
	/**
	 * Go short and block until the transaction completes.
	 * 
	 * @param qty
	 * @param timeout
	 * @param policy
	 */
	void shortMarket( BigDecimal qty, long timeout, OrderTimeoutPolicy policy);
	
	/**
	 * Go short using the Trade's default timeout time and OrderTimeoutPolicy.
	 * 
	 * @param qty
	 * @param block
	 */
	void shortMarket( BigDecimal qty, boolean block );
	
	/**
	 * Reduce a position.
	 * 
	 * @param qty	the quantity must be less than Trade.getQuantity()
	 * @param timeout
	 */
	void reduceMarket( BigDecimal qty, long timeout, OrderTimeoutPolicy policy, boolean block);
	
	/**
	 * Reduce a position and block until the transaction completes.
	 * 
	 * @param qty
	 * @param timeout
	 * @param policy
	 */
	void reduceMarket( BigDecimal qty, long timeout, OrderTimeoutPolicy policy);
	
	/**
	 * Reduce a position using the Trade's default timeout time and OrderTimeoutPolicy.
	 * 
	 * @param qty
	 * @param block
	 */
	void reduceMarket( BigDecimal qty, boolean block);
	
	/**
	 * Augment a position.
	 * 
	 * @param qty
	 * @param timeout
	 * @param policy
	 * @param block
	 */
	void augmentMarket( BigDecimal qty, long timeout, OrderTimeoutPolicy policy, boolean block);
	
	/**
	 * Augment a position and block until the transaction completes.
	 * 
	 * @param qty
	 * @param timeout
	 * @param policy
	 */
	void augmentMarket( BigDecimal qty, long timeout, OrderTimeoutPolicy policy);
	
	/**
	 * Augment a position using the Trade's default timeout time and OrderTimeoutPolicy.
	 * 
	 * @param qty
	 * @param block
	 */
	void augmentMarket( BigDecimal qty, boolean block);

}


























