package org.kohera.metctools.delegate;

import org.kohera.metctools.DelegatorStrategy;

public interface OtherDelegate extends EventDelegate {

	public void onOther( DelegatorStrategy sender, Object message );
	
}
