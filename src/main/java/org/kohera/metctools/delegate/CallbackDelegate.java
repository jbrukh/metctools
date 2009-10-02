package org.kohera.metctools.delegate;

import org.kohera.metctools.AdvancedStrategy;

/**
 * Delegate interface for Callback events. (For completeness.)
 * @author Administrator
 *
 */
public interface CallbackDelegate extends EventDelegate {

	public void onCallback( AdvancedStrategy sender, Object message );
	
}
