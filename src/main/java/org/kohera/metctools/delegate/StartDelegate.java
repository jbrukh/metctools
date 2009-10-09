package org.kohera.metctools.delegate;

import org.kohera.metctools.DelegatorStrategy;

public interface StartDelegate extends EventDelegate {

	public void onStart( DelegatorStrategy sender);
	
}
