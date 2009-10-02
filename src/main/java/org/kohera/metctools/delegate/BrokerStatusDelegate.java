package org.kohera.metctools.delegate;

import org.kohera.metctools.AdvancedStrategy;
import org.marketcetera.client.brokers.BrokerStatus;

/**
 * Delegate interface for BrokerStatus events.
 * 
 * @author Administrator
 *
 */
public interface BrokerStatusDelegate extends EventDelegate {

	public void onBrokerStatus( AdvancedStrategy sender, BrokerStatus status );
	
}
