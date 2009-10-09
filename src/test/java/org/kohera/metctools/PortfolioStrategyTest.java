package org.kohera.metctools;

import org.kohera.metctools.delegate.StartDelegate;
import org.kohera.metctools.delegate.TradeDelegate;
import org.kohera.metctools.portfolio.Trade;
import org.marketcetera.client.ClientInitException;
import org.marketcetera.event.TradeEvent;
import org.marketcetera.trade.BrokerID;
import org.marketcetera.trade.MSymbol;

public class PortfolioStrategyTest extends PortfolioStrategy
		implements StartDelegate, TradeDelegate {

	public PortfolioStrategyTest() throws ClientInitException {
		super();
		addDelegate(this);
	}

	@Override
	public void onStart(DelegatorStrategy sender) {
		setDataProvider(getParameter("DATA"));
		info("Hello world.");
		
		Trade t = new Trade(
				this,
				new MSymbol("AAPL"),
				new BrokerID("tos_qa"), 
				"08132009" 
				);
		
		getPortfolio().addTrade(t);
		startMarketData();
		
		
	}

	@Override
	public void onTrade(DelegatorStrategy sender, TradeEvent tradeEvent) {
		info("trade -- " + tradeEvent);
	}

}
