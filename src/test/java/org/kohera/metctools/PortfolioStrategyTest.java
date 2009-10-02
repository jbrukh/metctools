package org.kohera.metctools;

import org.marketcetera.client.ClientInitException;

public class PortfolioStrategyTest extends PortfolioStrategy {

	public PortfolioStrategyTest(String dataProvider)
			throws ClientInitException {
		super();
		
	}

	@Override
	public String returnDataProvider() {
		return "marketcetera".intern();
	}

}
