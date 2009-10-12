package org.kohera.metctools.delegate;

import org.kohera.metctools.DelegatorStrategy;
import org.marketcetera.client.brokers.BrokerStatus;

/**
 * Delegate interface for BrokerStatus events.
 * 
 * @author Administrator
 *
 */
public interface BrokerStatusDelegate extends EventDelegate {

	public void onBrokerStatus( DelegatorStrategy sender, BrokerStatus status );
	
}
