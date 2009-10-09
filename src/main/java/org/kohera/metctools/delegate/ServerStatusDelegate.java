package org.kohera.metctools.delegate;

import org.kohera.metctools.DelegatorStrategy;

public interface ServerStatusDelegate extends EventDelegate {
	
	public void onServerStatus( DelegatorStrategy sender, boolean status );

}
