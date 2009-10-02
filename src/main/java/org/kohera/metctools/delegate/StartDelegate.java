package org.kohera.metctools.delegate;

import org.kohera.metctools.AdvancedStrategy;

public interface StartDelegate extends EventDelegate {

	public void onStart( AdvancedStrategy sender);
	
}
