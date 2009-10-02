package org.kohera.metctools.delegate;

import org.kohera.metctools.AdvancedStrategy;
import org.marketcetera.event.AskEvent;

/**
 * Delegate interface for AskEvents.
 * @author Administrator
 *
 */
public interface AskDelegate {
	
	public void onAsk( AdvancedStrategy sender, AskEvent askEvent );


}
