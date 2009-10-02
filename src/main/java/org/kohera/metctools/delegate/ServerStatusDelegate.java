package org.kohera.metctools.delegate;

import org.kohera.metctools.AdvancedStrategy;

public interface ServerStatusDelegate extends EventDelegate {
	
	public void onServerStatus( AdvancedStrategy sender, boolean status );

}
