package org.kohera.metctools.delegate;

import org.kohera.metctools.DelegatorStrategy;

/**
 * Delegate for ServerStatus events.
 * 
 * @author Jake Brukhman
 *
 */
public interface ServerStatusDelegate extends EventDelegate {
	
	/**
	 * 
	 * Implement this method to handle server status events.
	 * 
	 * @param sender
	 * @param status
	 */
	public void onServerStatus( DelegatorStrategy sender, boolean status );

}
