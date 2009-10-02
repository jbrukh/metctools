package org.kohera.metctools.delegate;

import org.kohera.metctools.AdvancedStrategy;

public interface OtherDelegate extends EventDelegate {

	public void onOther( AdvancedStrategy sender, Object message );
	
}
