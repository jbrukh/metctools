package org.kohera.metctools.delegate;

import org.kohera.metctools.DelegatorStrategy;
import org.marketcetera.client.brokers.BrokerStatus;

/**
 * Delegate interface for BrokerStatus events.
 * 
 * @author Jake Brukhman
 *
 */
public interface BrokerStatusDelegate extends EventDelegate {

	/**
	 * 
	 * Implement this method to handle BrokerStatus events.
	 * 
	 * @param sender
	 * @param status
	 */
	public void onBrokerStatus( DelegatorStrategy sender, BrokerStatus status );
	
}
