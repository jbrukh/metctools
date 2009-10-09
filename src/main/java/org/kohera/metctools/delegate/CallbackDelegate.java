package org.kohera.metctools.delegate;

import org.kohera.metctools.DelegatorStrategy;

/**
 * Delegate interface for Callback events. (For completeness.)
 * @author Administrator
 *
 */
public interface CallbackDelegate extends EventDelegate {

	public void onCallback( DelegatorStrategy sender, Object message );
	
}
