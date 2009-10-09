package org.kohera.metctools.delegate;

import org.kohera.metctools.DelegatorStrategy;

public interface StopDelegate extends EventDelegate {

	public void onStop( DelegatorStrategy sender );
	
}
