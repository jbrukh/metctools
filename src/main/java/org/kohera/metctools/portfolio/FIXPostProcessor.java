package org.kohera.metctools.portfolio;

import org.marketcetera.trade.OrderSingle;

public interface FIXPostProcessor {

	public void postProcess( OrderSingle order );
	
}
