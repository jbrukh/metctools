package org.kohera.metctools.delegate;

import org.kohera.metctools.DelegatorStrategy;

/**
 * Delegate interface for Other events.
 * 
 * @author Jake Brukhman
 *
 */
public interface OtherDelegate extends EventDelegate {

	/**
	 * 
	 * Implement this method to handle Other events.
	 * 
	 * @param sender
	 * @param message
	 */
	public void onOther( DelegatorStrategy sender, Object message );
	
}
