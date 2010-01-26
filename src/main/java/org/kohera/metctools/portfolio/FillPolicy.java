package org.kohera.metctools.portfolio;

import java.io.Serializable;

import org.kohera.metctools.DelegatorStrategy;
import org.marketcetera.trade.OrderID;

/**
 * 
 * Interface for custom policies to be performed upon a fill of a
 * position.
 * 
 * @author Jake Brukhman
 *
 */
public interface FillPolicy extends Serializable {
	
	/**
	 * 
	 * Implement this method to handle fills of positions.
	 * 
	 * @param sender
	 * @param orderId
	 * @param trade
	 */
	public void onFill( DelegatorStrategy sender, OrderID orderId, Trade trade );

}
