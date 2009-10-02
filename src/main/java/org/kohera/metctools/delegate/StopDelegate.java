package org.kohera.metctools.delegate;

import org.kohera.metctools.AdvancedStrategy;

public interface StopDelegate extends EventDelegate {

	public void onStop( AdvancedStrategy sender );
	
}
